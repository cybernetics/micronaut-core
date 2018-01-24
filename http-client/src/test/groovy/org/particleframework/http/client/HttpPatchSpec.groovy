/*
 * Copyright 2018 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.http.client

import groovy.transform.EqualsAndHashCode
import io.reactivex.Flowable
import org.particleframework.context.ApplicationContext
import org.particleframework.http.HttpRequest
import org.particleframework.http.HttpResponse
import org.particleframework.http.HttpStatus
import org.particleframework.http.MediaType
import org.particleframework.http.annotation.Body
import org.particleframework.http.annotation.Controller
import org.particleframework.http.annotation.Header
import org.particleframework.runtime.server.EmbeddedServer
import org.particleframework.http.annotation.Patch
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class HttpPatchSpec extends Specification {

    @Shared @AutoCleanup ApplicationContext context = ApplicationContext.run()
    @Shared EmbeddedServer embeddedServer = context.getBean(EmbeddedServer).start()
    @Shared @AutoCleanup HttpClient client = context.createBean(HttpClient, embeddedServer.getURL())

    void "test simple post request with JSON"() {
        given:
        def book = new Book(title: "The Stand", pages: 1000)

        when:
        Flowable<HttpResponse<Book>> flowable = Flowable.fromPublisher(client.exchange(
                HttpRequest.PATCH("/patch/simple", book)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        ))
        HttpResponse<Book> response = flowable.blockingFirst()
        Optional<Book> body = response.getBody()

        then:
        response.status == HttpStatus.OK
        response.contentType.get() == MediaType.APPLICATION_JSON_TYPE
        response.contentLength == 34
        body.isPresent()
        body.get() instanceof Book
        body.get() == book
    }

    void "test simple post request with URI template and JSON"() {
        given:
        def book = new Book(title: "The Stand",pages: 1000)
        when:
        Flowable<HttpResponse<Book>> flowable = Flowable.fromPublisher(client.exchange(
                HttpRequest.PATCH("/patch/title/{title}", book)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        ))
        HttpResponse<Book> response = flowable.blockingFirst()
        Optional<Book> body = response.getBody()

        then:
        response.status == HttpStatus.OK
        response.contentType.get() == MediaType.APPLICATION_JSON_TYPE
        response.contentLength == 34
        body.isPresent()
        body.get() instanceof Book
        body.get().title == 'The Stand'
    }


    void "test simple post request with Form data"() {
        given:
        def book = new Book(title: "The Stand", pages: 1000)
        when:
        Flowable<HttpResponse<Book>> flowable = Flowable.fromPublisher(client.exchange(
                HttpRequest.PATCH("/patch/form", book)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        ))
        HttpResponse<Book> response = flowable.blockingFirst()
        Optional<Book> body = response.getBody()

        then:
        response.status == HttpStatus.OK
        response.contentType.get() == MediaType.APPLICATION_JSON_TYPE
        response.contentLength == 34
        body.isPresent()
        body.get() instanceof Book
        body.get().title == 'The Stand'
    }

    void "test simple post retrieve blocking request with JSON"() {
        given:
        def toSend = new Book(title: "The Stand",pages: 1000)
        when:
        BlockingHttpClient blockingHttpClient = client.toBlocking()
        Book book = blockingHttpClient.retrieve(
                HttpRequest.PATCH("/patch/simple", toSend)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

                Book
        )

        then:
        book == toSend
    }


    @Controller('/patch')
    static class PostController {

        @Patch('/simple')
        Book simple(@Body Book book, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert contentType == MediaType.APPLICATION_JSON
            assert contentLength == 34
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }

        @Patch('/title/{title}')
        Book title(@Body Book book, String title, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert title == book.title
            assert contentType == MediaType.APPLICATION_JSON
            assert contentLength == 34
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }

        @Patch(uri = '/form', consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Book form(@Body Book book, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            assert contentType == MediaType.APPLICATION_FORM_URLENCODED
            assert contentLength == 93
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }
    }
    @EqualsAndHashCode
    static class Book {
        String title
        Integer pages
    }
}


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
package example.api.v1;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.particleframework.http.annotation.Body;
import org.particleframework.http.annotation.Get;
import org.particleframework.http.annotation.Post;
import org.particleframework.validation.Validated;

import javax.validation.Valid;
import java.util.List;

/**
 * @author graemerocher
 * @since 1.0
 */
@Validated
public interface PetOperations<T extends Pet> {

    @Get("/")
    Single<List<T>> list();

    @Get("/vendor/{name}")
    Single<List<T>> byVendor(String name);

    @Get("/{slug}")
    Maybe<T> find(String slug);

    @Post("/")
    Single<T> save(@Valid @Body T pet);
}

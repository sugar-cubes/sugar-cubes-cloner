/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Java Reflection Cloner library module. See {@link io.github.sugarcubes.cloner} package description.
 *
 * @see io.github.sugarcubes.cloner
 *
 * @author Maxim Butov
 */
module io.github.sugarcubes.cloner {
    requires static jdk.unsupported;
    requires static org.objenesis;
    exports io.github.sugarcubes.cloner;
}

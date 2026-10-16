/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.cache;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link StandardCacheKeyGenerator} 参数分界、类型区分与集合规范化的碰撞验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class StandardCacheKeyGeneratorTest {

    private final StandardCacheKeyGenerator generator = new StandardCacheKeyGenerator();

    @Test
    void parameterBoundariesProduceDistinctKeys() throws Exception {
        Method method = String.class.getMethod("length");
        assertNotEquals(generator.generate(this, method, "ab", "c"),
                generator.generate(this, method, "a", "bc"));
        assertNotEquals(generator.generate(this, method, "1", "23"),
                generator.generate(this, method, "12", "3"));
    }

    @Test
    void nullAndLiteralNullStringAreDistinct() throws Exception {
        Method method = String.class.getMethod("length");
        assertNotEquals(generator.generate(this, method, (Object) null),
                generator.generate(this, method, "null"));
    }

    @Test
    void sameTextDifferentTypesAreDistinct() throws Exception {
        Method method = String.class.getMethod("length");
        assertNotEquals(generator.generate(this, method, 1),
                generator.generate(this, method, "1"));
    }

    @Test
    void collectionCanonicalOrderIsStable() throws Exception {
        Method method = String.class.getMethod("length");
        String ordered = generator.generate(this, method, List.of("a", "b"));
        assertEquals(ordered, generator.generate(this, method, List.of("b", "a")));
        assertEquals(ordered, generator.generate(this, method, Set.of("b", "a")));
    }

    @Test
    void collectionDiffersFromScalar() throws Exception {
        Method method = String.class.getMethod("length");
        assertNotEquals(generator.generate(this, method, List.of("a")),
                generator.generate(this, method, "a"));
    }

    @Test
    void mapCanonicalOrderIsStable() throws Exception {
        Method method = String.class.getMethod("length");
        Map<String, Object> forward = new LinkedHashMap<>();
        forward.put("a", 1);
        forward.put("b", 2);
        Map<String, Object> reversed = new LinkedHashMap<>();
        reversed.put("b", 2);
        reversed.put("a", 1);
        assertEquals(generator.generate(this, method, forward),
                generator.generate(this, method, reversed));
    }

    @Test
    void noArgsAndDeterministicKeys() throws Exception {
        Method method = String.class.getMethod("length");
        assertEquals("length:noargs", generator.generate(this, method));
        assertEquals(generator.generate(this, method, "a", "b"),
                generator.generate(this, method, "a", "b"));
    }
}

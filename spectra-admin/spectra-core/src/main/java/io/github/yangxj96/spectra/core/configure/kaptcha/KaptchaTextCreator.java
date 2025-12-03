/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.kaptcha;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.Random;

/**
 * 验证码文本生成器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
public class KaptchaTextCreator extends DefaultTextCreator {

    // 包含的数字
    private static final String[] NUMBERS = "0,1,2,3,4,5,6,7,8,9,10".split(",");

    // 随机数种子
    private static final Random random = new Random();

    @Override
    public String getText() {

        var x = random.nextInt(10);
        var y = random.nextInt(10);
        var operands = random.nextInt(3);

        // 使用 switch 表达式（JDK 14+）返回结果
        var result = switch (operands) {
            case 0 -> x * y;
            case 1 -> {
                if (x != 0 && y % x == 0) {
                    yield y / x;
                } else {
                    yield x + y;
                }
            }
            case 2 -> Math.abs(x - y);  // 可简化为表达式
            default -> throw new IllegalArgumentException("操作数越界: " + operands);
        };

        // 使用 StringBuilder 构建表达式
        var expression = switch (operands) {
            case 0 -> String.format("%s*%s", NUMBERS[x], NUMBERS[y]);
            case 1 -> (x != 0 && y % x == 0) ?
                    String.format("%s/%s", NUMBERS[y], NUMBERS[x]) :
                    String.format("%s+%s", NUMBERS[x], NUMBERS[y]);
            case 2 -> (x >= y) ?
                    String.format("%s-%s", NUMBERS[x], NUMBERS[y]) :
                    String.format("%s-%s", NUMBERS[y], NUMBERS[x]);
            default -> throw new IllegalStateException();
        };

        return expression + "=?@" + result;
    }

}

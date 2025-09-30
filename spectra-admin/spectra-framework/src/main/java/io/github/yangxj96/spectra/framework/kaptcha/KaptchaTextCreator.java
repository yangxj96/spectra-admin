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

package io.github.yangxj96.spectra.framework.kaptcha;

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
        int result;

        int x = random.nextInt(10);
        int y = random.nextInt(10);
        StringBuilder suChinese = new StringBuilder();
        int operands = random.nextInt(3);
        switch (operands) {
            case 0: {
                result = x * y;
                suChinese.append(NUMBERS[x]);
                suChinese.append("*");
                suChinese.append(NUMBERS[y]);
                break;
            }
            case 1: {
                if ((x != 0) && y % x == 0) {
                    result = y / x;
                    suChinese.append(NUMBERS[y]);
                    suChinese.append("/");
                    suChinese.append(NUMBERS[x]);
                } else {
                    result = x + y;
                    suChinese.append(NUMBERS[x]);
                    suChinese.append("+");
                    suChinese.append(NUMBERS[y]);
                }
                break;
            }
            case 2: {
                if (x >= y) {
                    result = x - y;
                    suChinese.append(NUMBERS[x]);
                    suChinese.append("-");
                    suChinese.append(NUMBERS[y]);
                } else {
                    result = y - x;
                    suChinese.append(NUMBERS[y]);
                    suChinese.append("-");
                    suChinese.append(NUMBERS[x]);
                }
                break;
            }
            default:
                throw new IndexOutOfBoundsException("操作数数据越界");
        }
        suChinese.append("=?@").append(result);
        return suChinese.toString();
    }

}

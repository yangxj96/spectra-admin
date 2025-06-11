/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Copyright (c) 2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Copyright (c) 2018 - 2025
 * 作者：杨新杰(Jack Young)
 * 邮箱：yangxj96@gmail.com
 * 博客：www.yangxj96.com
 * 日期：2025-06-11 16:44:11
 * Copyright (c) 2018 - 2025
 */

/*
 *
 *  * Copyright (c) 2018 - 2025 杨新杰(Jack Young)
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

/*
 *
 *  * Copyright (c) 2018 - 2025 杨新杰(Jack Young)
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

/*
 * Copyright (c) 2018 - 2025
 * 作者：杨新杰(Jack Young)
 * 邮箱：yangxj96@126.com
 * 博客：www.yangxj96.com
 * 日期：2025-06-11 16:37:12
 * Copyright (c) 2018 - 2025
 */

/*
 * Copyright (c) 2018 - 2025
 * 作者：杨新杰(Jack Young)
 * 邮箱：yangxj96@gmail.com
 * 博客：www.yangxj96.com
 * 日期：2025-06-11 16:35:35
 * Copyright (c) 2018 - 2025
 */

-- 默认账号数据
INSERT INTO db_system.t_account (id, username, password, enable, created_by, created_at, updated_by, updated_at, deleted) VALUES (1927290201865945090, 'sysadmin', '$2a$10$ALzuYNgOSYLlJg/XsxUY7O4BKeqECHf5J7bY8eGPaQK.3VSlkFTaO', true, null, null, null, null, null);
-- 菜单初始化
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929928379575111682, 0, 'icon-setting', '系统管理', '/system', 'layout', 'layout', 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929928379667386370, 0, 'icon-setting', '组件示例', '/example', 'layout', 'layout', 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526785, 1929928379575111682, 'icon-setting-role', '角色管理', 'role', '/System/Role/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526787, 1929928379575111682, 'icon-setting-role', '字典管理', 'dict', '/System/Dict/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620816441347, 1929928379667386370, 'icon-setting-role', '列表示例', 'table', '/Example/Table/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526789, 1929928379575111682, 'icon-setting-role', '菜单管理', 'menu', '/System/Menu/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526786, 1929928379575111682, 'icon-setting-role', '权限管理', 'power', '/System/Power/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526788, 1929928379575111682, 'icon-setting-role', '定时任务', 'task', '/System/Task/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620816441348, 1929928379667386370, 'icon-setting-role', '表单示例', 'form', '/Example/Form/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620816441346, 1929928379667386370, 'icon-setting-role', '图表示例', 'echarts', '/Example/Echarts/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620753526790, 1929928379575111682, 'icon-setting-role', '文件存储', 'storage', '/System/Storage/index', null, 0, null, null, null, null, null);
INSERT INTO db_system.t_menu (id, pid, icon, name, path, component, layout, sort, created_by, created_at, updated_by, updated_at, deleted) VALUES (1929929620715778049, 1929928379575111682, 'icon-setting-role', '用户管理', 'user', '/System/User/index', null, 0, null, null, null, null, null);

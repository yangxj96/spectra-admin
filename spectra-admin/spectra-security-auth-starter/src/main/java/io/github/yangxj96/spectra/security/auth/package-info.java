/**
 * 职责说明:
 *
 * <pre>
 *     <ul>
 *         <li>定义一个 Authentication 实现</li>
 *         <li>定义一个 Filter，把 token 解析成 Authentication</li>
 *         <li>把 Authentication 放进 SecurityContext</li>
 *         <li>自动装配到 Spring Security</li>
 *     </ul>
 * </pre>
 */
package io.github.yangxj96.spectra.security.auth;
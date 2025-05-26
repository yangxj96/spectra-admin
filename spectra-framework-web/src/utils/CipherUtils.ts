// 公钥:3059301306072a8648ce3d020106082a811ccf5501822d0342000409909a0b8504ef16988cc7e74278c15d44aced900df78a03c12a2d88ca15147a06d0afcb3747b41d26234e807fa22ae1a5c88bb9d7e86e612684452b4e8e5313
// 私钥:308193020100301306072a8648ce3d020106082a811ccf5501822d0479307702010104200cb990a680f3de34944f15e5b9183d3172c25a1cd5ab7dd0ba5feeb441fe1240a00a06082a811ccf5501822da1440342000409909a0b8504ef16988cc7e74278c15d44aced900df78a03c12a2d88ca15147a06d0afcb3747b41d26234e807fa22ae1a5c88bb9d7e86e612684452b4e8e5313
// 加密结果(列表):RCipher(code=0, msg=请求成功, data=643f94b895e5d4ff2a7077931ab09e4a1030927f2874b51bb15053fe0e92d1fed9ace4b84fa9b6b81166f87e92b391f24abece6510a3454fe7a4094cb638100b5fc046819677a001e5db441651cc209d32183dcd5cef2fce44fe31a4e589e1058893b2d44f800c72c6f94eeb15a805ceb5e3deb97b0b515bd995153ad2a462cc, digest=XCFnDaevandh23uUiolach1P/EoobK8vr74v6bh44M8=, sign=MEYCIQDFfnHmbCuo5VVloktM7lbyuCJNHabfe4LrfZDT7c5BcgIhAPxxR3xJlCXbagz7+o9P7iVcK+g3NGmogSFbFNoapaGr)
// 解密结果(列表):[User(username=test-0, password=test-0, accountNonExpired=null, accountNonLocked=null, enabled=null, credentialsNonExpired=null), User(username=test-1, password=test-1, accountNonExpired=null, accountNonLocked=null, enabled=null, credentialsNonExpired=null), User(username=test-2, password=test-2, accountNonExpired=null, accountNonLocked=null, enabled=null, credentialsNonExpired=null)]
// 加密结果(单条):RCipher(code=0, msg=请求成功, data=33648a2c63ba7eb02f2b3fc07abb954b04e4e3ebe3fe95d4d7c8242c0e3c02efd8629a5a6a8f7b2db733b5c2c74811d8, digest=4V/WDRr+3j2X++BqR+oEnG/f5xpYpp2nxbpxIsSLrgg=, sign=MEYCIQDb/5QVEctBkU+A4fJ6yNKFfpgQEPQLbfcLYEqBibqRGgIhAPfXrMreQnRLFiR6QKPVsJWxTnMx0HWnYY7NCPYmr1/v)
// 解密结果(单条):User(username=username, password=password, accountNonExpired=null, accountNonLocked=null, enabled=null, credentialsNonExpired=null)

import { sm2 } from "sm-crypto-v2";

const publicKey =
    "0454c9ec4e9cb6ea61897b28519fe0c5b81b710df76a7c1ae73550acd49399d899bba3671a465177fe065c99be75038066e39b2f3ddff2856372297378524db215";

const privateKey = "597a116a0a01a717573702cff0f0c6e07c67561704ff773a00d94c23b01ee250";

const sign = "MEYCIQCywt9CsS5TO6GJLnqNAouWhqs/LOEfBB5vFX5aP0LH1QIhAI6tJ+YYCdLr6d8UDDACzM6heXUUt+gQsVz5BNWe/FFu";

export function encrypt() {}

export function decrypt() {
    console.log(sm2.verifyPublicKey(publicKey));
    let flag = sm2.doVerifySignature("Hello World", sign, publicKey, { hash: true, der: true });
    console.log("验证结果:", flag);
}

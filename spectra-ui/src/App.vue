<script setup lang="ts">
import { reactive, ref } from "vue";

import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";
import { decrypt, verifySignature } from "@/utils/crypto-utils.ts";

import type { Language } from "element-plus/es/locale";

const locale = ref(useAppStore().lang as Language);
const message = reactive({
    max: 3,
    duration: 500,
    plain: true,
    appendTo: ".box-content"
});

// RSA 公钥（用于验签）
const PUBLIC_KEY =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4gL70h/9lRXO4lGA+q+zV9lduMSqFfm7rdSjlUnFK0hYb7N//+4P3Z9XQyC0WIWHgT3UkQHSMxPzkq+V7LFuCPm0bzp6RV1o3TmALgDw96NbyKq2v//W+SAYl4hBvnHv6r/dsglA+WAjxEKKCka25vUD6Az8uRXkJPzZ3x6dJtnae212foe6/cQumVs++zdIR1QcpTScX2zX726My/HkPCy+8BdWlbIhaZ7qAZMM8A78PY0Fd4HduX0EJQFk6uRJhSsE/rBLkiiS19Ji2n6Dy3+bzid7F31PKWzuMjepSEuzi7W+B5hQgZhcd8RXuuReIsyx7NdRIAQ/9F26BGtDmwIDAQAB";

// RSA 私钥（用于解密，生产环境建议由后端解密）
const PRIVATE_KEY =
    "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDiAvvSH/2VFc7iUYD6r7NX2V24xKoV+but1KOVScUrSFhvs3//7g/dn1dDILRYhYeBPdSRAdIzE/OSr5XssW4I+bRvOnpFXWjdOYAuAPD3o1vIqra//9b5IBiXiEG+ce/qv92yCUD5YCPEQooKRrbm9QPoDPy5FeQk/NnfHp0m2dp7bXZ+h7r9xC6ZWz77N0hHVBylNJxfbNfvbozL8eQ8LL7wF1aVsiFpnuoBkwzwDvw9jQV3gd25fQQlAWTq5EmFKwT+sEuSKJLX0mLafoPLf5vOJ3sXfU8pbO4yN6lIS7OLtb4HmFCBmFx3xFe65F4izLHs11EgBD/0XboEa0ObAgMBAAECggEAXqRYNiPuSOVSdmQh6hf0NlfQDsTP4pbql/7Ap422Mos/eTsF4TKb0zbb0EDcNGtqFF1Da5uDHZ9/sk+zrOOZy8lg3Oj0daKJfIYNHnaHcXOnRi7yixEDwNvHYIEO4sSMblV87QZB/otbPt6MNoRpJ+q3AY+arpizqid1xqBnt3HePYnzxvXyYAxBMYkWqYFfpdRiJJnKK4+c9Pebrn48C8lspP6ideplH+k6F52cs0p7/6WhnEX9JQtOBVncCBDrmE1JQjpcJ+nEBOaUeDW5tCzbKK5u+NFukWHJpzsM3xx3ptyBOYo/vHCNAKRqnU9QDxQq1nj8lWi6bhK1G0JiIQKBgQDustyQo1PYkygL/i1HVN9bh1f/6hMsRVa5z7P0cWAnhlIWtYNBxmAfvQAo6on2Z+ePQS1IgroX1KWf6LvYxeOC9rbaWx5BSnmBh0F97FpUu+4VIdqVE7ZORVPVYErHpx9gLiEXiZXT2fpimYmBrrfbkLtNmTOWQ6CIDdChiz76YQKBgQDyZLW7cjc5uWxSKeA840xx6K3VnMzoSd4kr4TIoGbBwIkGtfhSucX+Y00qJXIcpb3TwQbCFTuBOnJ/G9MO5Q259OJA32ovrwvccMtQTOjyD0KT8mH5aReqfWGcHXRNqcxyhzdDrqglFYBTpDREQkAdVjFCDHnXQwG7L50KY1JXewKBgE1sFGCRqVuA96dULUmKvNvKPX/TY56jaurf9MWflImdZrjY7RcDiiAhRJEA6fjTYOc1Q8PWKxgZsBL7ARrhrNbIwSx4C4sv5dNelJ6McAw13JZcIbLi7unaUMPP9NFnYqTmsXMZ/E/3zt/2XwSCMD52ZlpYC8R8cPBQUtBjAu1hAoGAexW2zsN11aMZpTNMgb6uL92GgEVKK95uplRWagZBh7t8GWNIAMIDD2VfZrpzEqb7GM8Y3m8nszx5rZ/0ccjQSpBWX7jTU/lgXYUNSO51D42+MiBPrfW2pcVbTooKZ/GENtgAsSa8jXA4pMBqJgs6QWtmQfLR4ARPmbzY0tCSnIcCgYBFzpaTlhquybTMkr9hRzsheNOznfrqQk4w+7WooBHP8JCWdZyIYgu1Jn0IM1z2GRzF3qYEiEwYnIp3daC1ryuIHHkjYSE34EkBn1lIVBlypFZtd1ZJQ1ZFljpH6t7Irxg0SJDztJlZ3Ywvk0XbUDNB9pzmIQx/5c9pdy3gqdUpMQ==";

const data = {
    data: "Di/5fRHfgmQ0dnLZbd9tkqTCHFWPsHyGRn+c8FEaR7BhQ+XKJzz1jcTLD6baiNX7wcLyKzZL0HQQTHz+PpjtFLF+GLknWKjO+KGkpbJA1XSF9DeuIdJlN9WKI7/+I28GnNx5Fid5cvZD3wZI4hzu0Sy4E31j9cP1/6Mg5sy6n1iM5AYR89HsDvoLHKA84MtCdh1EqXrI6Q8TqD+Y0ddl2B5MvE995BLusp4+sDmxx76aB2LKdDUYF4MhVcFkm34BPQl6AeUnzmc1gN9so/llhh4Acmv3vLkKyJUClabK1g5c4+WMleNT1QMWECOSyBmahlpeiKiDWQw=",
    signature:
        "TP53FUGsPHcp41mNgvLvy0DkyRnkUsEyfc10bKCihlq0Y2X0YDwfQ+of82iGudxVsInG+uED6Zj52yedvG6edAJncMj34XkTU19/VXTdzgD4icxfeeJbA6iLutyWMReuUAvtO861B1higfOLStf72U7aSJuEv4p6qOh5bQY9iPwg6b9Lh0kMjFNrB2oFt1IcFvnvruIqbZGdDqAKazDy8/eUhd7EM2OOFzbooE9TRAyMYKYtVHS2gn2UeULE0WJuMJdn05TcHCIJnIPxKiG9lWpPYOvcX8Xn1wKJkkUPgjYfX44uN8o7mlH+EgGFGssASMw8Pdlqyt5YJuz/OuUiFA==",
    iv: "e72c5b732b70ee17c69572f1",
    nonce: "EcIKGNw+fCdxCgRm5G0Pkg==",
    key: "E+Anvm1AxnIqj+t/1bz4lENkLwJbnF9hjnEWmtAOj4Ev5sdxGA9SGYT83c2dfs/Kw8f8rgCk5/6d7Kl6hgnCOh55Wfq6s8l9fa1Nr7KSxXZLlT5UkJUCKIioNSr4LZhl4aUMDVuZ6Pm+KPPNIg9RJNA9crC/5Ti1rImf4jEcuQOQC1pdUm6/37IJ9jIybl5XvwUNuvZQugwyQoyzvCVJvAvonS1d8e+EIqNjofCjlMvCp5dlkI65ynPTelTDu+3s8gphJDs2ZngdfTe09IJN7O8HO6asgbZJox9ke26oKNj9kGiMc5LjQUSzn03bYNbwlAqA/FH4Uwd7fypQqJxUwQ==",
    timestamp: 1780565985
};

const xx = async () => {
    const signData = `data=${data.data}&nonce=${data.nonce}&timestamp=${data.timestamp}`;
    const isValid = await verifySignature(data.signature, signData, PUBLIC_KEY);
    if (!isValid) {
        throw new Error("签名验证失败，数据可能被篡改");
    }
    console.log("✅ 签名验证通过");
    // 第二步：RSA 解密 AES 密钥 + AES-GCM 解密业务数据
    const result = await decrypt(
        data.key, // RSA 加密的 AES 密钥（Base64）
        data.data, // AES-GCM 加密的业务数据（Base64）
        data.iv, // IV（HEX 格式）
        PRIVATE_KEY // RSA 私钥（Base64）
    );
    console.log("解密成功:", JSON.parse(result));
};

xx();
</script>

<template>
    <div id="nav">
        <el-config-provider :locale="locale" :message="message">
            <router-view />
        </el-config-provider>
    </div>
</template>

<style lang="scss">
@use "@/assets/css/common.scss";

* {
    padding: 0;
    margin: 0;
}
</style>

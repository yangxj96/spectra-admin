<script setup lang="ts">
import { DEFAULT_FEATURE } from "@/config/default";
import { useAuthGuard } from "@/hooks/useAuthGuard";

// #ifdef MP-WEIXIN
useAuthGuard();
// #endif
</script>

<template>
    <!-- 头部保留 -->
    <uni-nav-bar statusBar fixed title="工作台" />
    <!-- #ifdef APP -->
    <scroll-view style="flex: 1">
        <!-- #endif -->
        <uni-section title="常用" type="line">
            <t-grid
                class="t-grid badge"
                :hover="true"
                theme="card"
                v-for="(item, index) in 2"
                :index="index"
                :key="index">
                <t-grid-item
                    @click="
                        () => {
                            console.log('点击了功能项', index);
                        }
                    "
                    t-class-image="image"
                    :text="'标题文字' + index"
                    :image="DEFAULT_FEATURE"
                    :badge-props="{ count: 8 }"
                    v-for="(item, index) in 4"
                    :index="index"
                    :key="index" />
            </t-grid>
        </uni-section>

        <uni-section title="OA相关" type="line">
            <t-grid
                class="t-grid badge"
                :hover="true"
                theme="card"
                v-for="(item, index) in 2"
                :index="index"
                :key="index">
                <t-grid-item
                    @click="
                        () => {
                            console.log('点击了功能项', index);
                        }
                    "
                    t-class-image="image"
                    :text="'标题文字' + index"
                    :image="DEFAULT_FEATURE"
                    :badge-props="{ dot: true }"
                    v-for="(item, index) in 4"
                    :index="index"
                    :key="index" />
            </t-grid>
        </uni-section>

        <!-- #ifdef APP -->
    </scroll-view>
    <!-- #endif -->
</template>

<style lang="scss" scoped>
.image {
    width: 25px;
    height: 25px;
}

.text {
    font-size: 14px;
    margin-top: 5px;
}

.example-body {
    /* #ifndef APP-NVUE */
    // display: block;
    /* #endif */
}

.grid-dynamic-box {
    margin-bottom: 15px;
}

.grid-item-box {
    flex: 1;
    // position: relative;
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 15px 0;
}

.grid-item-box-row {
    flex: 1;
    // position: relative;
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: row;
    align-items: center;
    justify-content: center;
    padding: 15px 0;
}

.grid-dot {
    position: absolute;
    top: 5px;
    right: 15px;
}

.swiper {
    height: 420px;
}

/* #ifdef H5 */
@media screen and (min-width: 768px) and (max-width: 1425px) {
    .swiper {
        height: 630px;
    }
}

@media screen and (min-width: 1425px) {
    .swiper {
        height: 830px;
    }
}

/* #endif */
</style>

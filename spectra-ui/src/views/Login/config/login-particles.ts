import type { ISourceOptions } from "@tsparticles/engine";

// 登录也particles明亮模式配置
export const loginParticlesLight: ISourceOptions = {
    autoPlay: true,
    background: {
        color: "#0d47a1",
        image: "",
        position: "",
        repeat: "",
        size: "",
        opacity: 1
    },
    fullScreen: { enable: true, zIndex: -1 },
    detectRetina: true,
    fpsLimit: 120,
    interactivity: {
        detectsOn: "window",
        events: {
            onClick: { enable: true, mode: "push" },
            onHover: { enable: true, mode: "grab" },
            resize: { enable: true, delay: 0.5 }
        },
        modes: {
            grab: { distance: 250, links: { opacity: 0.5 } },
            push: { quantity: 4 },
            remove: { quantity: 2 },
            repulse: { distance: 200, duration: 0.4 }
        }
    },
    particles: {
        number: { value: 120 },
        color: { value: "#e0e0e0" },
        links: { enable: true, distance: 150, opacity: 0.4, color: "#90caf9" },
        move: { enable: true, speed: 0.8 },
        size: { value: { min: 1, max: 2 } }
    },
    pauseOnBlur: true,
    pauseOnOutsideViewport: true
};

// // 登录也particles暗黑模式配置
export const loginParticlesDark: ISourceOptions = {
    autoPlay: true,
    background: {
        color: "#0a1f2c", // 深蓝黑背景
        image: "",
        position: "",
        repeat: "",
        size: "",
        opacity: 1
    },
    fullScreen: { enable: true, zIndex: -1 },
    detectRetina: true,
    fpsLimit: 120,
    interactivity: {
        detectsOn: "window",
        events: {
            onClick: { enable: true, mode: "push" },
            onHover: { enable: true, mode: "grab" },
            resize: { enable: true, delay: 0.5 }
        },
        modes: {
            grab: { distance: 250, links: { opacity: 0.5 } },
            push: { quantity: 4 },
            remove: { quantity: 2 },
            repulse: { distance: 200, duration: 0.4 }
        }
    },
    particles: {
        number: { value: 120 },
        color: { value: ["#81d4fa", "#4fc3f7", "#26c6da"] },
        links: { enable: true, distance: 150, opacity: 0.4, color: "#81d4fa" },
        move: { enable: true, speed: 0.8 },
        size: { value: { min: 1, max: 2 } }
    },
    pauseOnBlur: true,
    pauseOnOutsideViewport: true
};

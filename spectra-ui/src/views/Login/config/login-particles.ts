import type { ISourceOptions } from "@tsparticles/engine";

/**
 * particles库实现的星空效果
 */
export const loginParticlesOptions: ISourceOptions = {
    autoPlay: true,
    background: {
        color: {
            value: "#020617"
        },
        image: "",
        position: "",
        repeat: "",
        size: "",
        opacity: 1
    },
    backgroundMask: {
        composite: "destination-out",
        cover: {
            opacity: 1,
            color: {
                value: ""
            }
        },
        enable: false
    },
    clear: true,
    defaultThemes: {},
    delay: 0,
    fullScreen: {
        enable: true,
        zIndex: -1
    },
    detectRetina: true,
    duration: 0,
    fpsLimit: 120,
    interactivity: {
        detectsOn: "window",
        events: {
            onClick: {
                enable: true,
                mode: "push"
            },
            onDiv: {
                selectors: "#login-page",
                enable: false,
                mode: "bubble",
                type: "circle"
            },
            onHover: {
                enable: true,
                mode: "grab",
                parallax: {
                    enable: true,
                    force: 60,
                    smooth: 10
                }
            },
            resize: {
                delay: 0.5,
                enable: true
            }
        },
        modes: {
            trail: {
                delay: 1,
                pauseOnStop: false,
                quantity: 1
            },
            attract: {
                distance: 200,
                duration: 0.4,
                easing: "ease-out-quad",
                factor: 1,
                maxSpeed: 50,
                speed: 1
            },
            bounce: {
                distance: 200
            },
            bubble: {
                distance: 400,
                duration: 2,
                mix: false,
                opacity: 0.8,
                size: 40,
                divs: {
                    distance: 200,
                    duration: 0.4,
                    mix: false,
                    selectors: {}
                }
            },
            connect: {
                distance: 80,
                links: {
                    opacity: 0.5
                },
                radius: 60
            },
            grab: {
                distance: 400,
                links: {
                    blink: false,
                    consent: false,
                    opacity: 1
                }
            },
            push: {
                default: true,
                groups: [],
                quantity: 4,
                particles: {}
            },
            remove: {
                quantity: 2
            },
            repulse: {
                distance: 200,
                duration: 0.4,
                factor: 100,
                speed: 1,
                maxSpeed: 50,
                easing: "ease-out-quad",
                divs: {
                    distance: 200,
                    duration: 0.4,
                    factor: 100,
                    speed: 1,
                    maxSpeed: 50,
                    easing: "ease-out-quad",
                    selectors: {}
                }
            },
            slow: {
                factor: 3,
                radius: 200
            },
            particle: {
                replaceCursor: false,
                pauseOnStop: false,
                stopDelay: 0
            },
            light: {
                area: {
                    gradient: {
                        start: {
                            value: "#ffffff"
                        },
                        stop: {
                            value: "#000000"
                        }
                    },
                    radius: 1000
                },
                shadow: {
                    color: {
                        value: "#000000"
                    },
                    length: 2000
                }
            }
        }
    },
    manualParticles: [],
    particles: {
        number: {
            value: 120
        },
        color: {
            value: ["#ffffff", "#60a5fa", "#a78bfa"]
        },
        links: {
            enable: true,
            distance: 120,
            opacity: 0.3
        },
        move: {
            enable: true,
            speed: 0.8
        },
        size: {
            value: { min: 1, max: 2 }
        }
    },
    pauseOnBlur: true,
    pauseOnOutsideViewport: true,
    responsive: [],
    smooth: false,
    style: {},
    themes: [],
    zLayers: 100,
    key: "parallax",
    name: "Parallax",
    motion: {
        disable: true,
        reduce: {
            factor: 4,
            value: true
        }
    }
};

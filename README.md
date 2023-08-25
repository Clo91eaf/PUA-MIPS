# PUA(Powerful Ultimate Architecture)mips。

[比赛总结 https://clo91eaf.github.io/c80a81d32b18/](https://clo91eaf.github.io/c80a81d32b18/)

## 环境配置
本Chisel项目使用sbt进行构建.
sbt版本为1.8.2
scala版本为2.13.8
Chisel版本为3.5.4

在chisel目录下执行
```shell
make verilog
```

执行完成即可在chisel/generated目录下生成PuaMips.v verilog文件.
chisel/src/main/resources/mycpu_top.v可作为顶层文件.

## 简介
本项目在《CPU设计实战》的基础上简单设计了一个高性能双发射六级流水mips处理器。初赛性能分62，频率为88MHz，ipc为1.25。
具体的设计情况请参阅doc/final-design.pdf。
该处理器可以运行linux6.5-rc3最新版内核。
该处理器所有的代码不超过5000行，应该为龙芯杯历史上代码量相当小的处理器。
该处理器有较大的可改进空间，欢迎大家参考，由于时间紧张，没有对频率进行太大的优化。
本项目已进入尾声，不再进行维护。

## 资源:
1. [Git commit message conventions](https://gitee.com/help/articles/4231#article-header0)
git的提交规范。

2. [Chisel-template](https://github.com/freechipsproject/chisel-template)。
pua-mips中的Chisel项目的初始化使用了这个模板。

3. [Online Assembler](https://godbolt.org/) 
在线的编译器,用来生成对应的mips二进制文件参考。

4. [MIPS Converter](https://www.eg.bucknell.edu/~csci320/mips_web/)
负责把mips指令转换为二进制,并且提供对应指令的详细说明。

5. [Chisel环境配置](https://clo91eaf.github.io/80b5fe4ebe03/)

## 杂项

### 正确参考本项目的方式

1. 请不要直接复制本项目的代码。
2. 在理解本项目模块的基础上，使用你自己的书写习惯进行代码实现，并尝试通过以下角度获得更好的代码实现。
   1. 代码的可读性。多写注释。
   2. 代码的可维护性。解耦，模块化。本项目中除了少部分文件的代码量超过200行，其他文件的代码量都在200行以内。
   3. 尽情释放Chisel语言的高度抽象能力。
   4. 撇弃软件思维，先设计，后实现。
3. 通过性能计数器比较性能。永远是数据指导优化。
4. 模拟器，仿真器。不要陷入波形的陷阱。不要安逸于现状。始终思考如何才能少看波形，如何获得更多的信息。

### 本项目的一些特点
你将在本项目中不可避免的看到：
1. 中英文注释混杂。
2. 代码风格不统一。
3. 奇怪的命名。
4. 莫名其妙的硬编码设计。
5. 冗余的提交记录。

我们在开发中已经尽可能的规避这些问题，并且进行了多次重构。但是由于时间紧张，我们无法保证代码的完美。

如果我们的设计过程对你有启发，我们将感到很高兴。
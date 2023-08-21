# PUA(Powerful Ultimate Architecture)mips。

## 环境配置
本Chisel项目使用sbt进行构建.
sbt版本为1.8.2
scala版本为2.13.8
Chisel版本为3.5.4

在Chisel目录下执行
```shell
make verilog
```

执行完成即可在chisel/generated目录下生成PuaMips.v verilog文件.
chisel/src/main/resources/mycpu_top.v可作为顶层文件.

## 简介
本项目在《CPU设计实战》的基础上简单设计了一个高性能双发射六级流水mips处理器。性能分62，频率为88MHz。
具体的情况请参阅doc/final-design.pdf。
该处理器可以运行linux6.5-rc3最新版内核。
该处理器有较大的可改进空间，欢迎大家参考，由于时间紧张，没有对频率进行太大的优化。本处理器核在频率上的优化空间还有很大。
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

5. [环境配置](https://clo91eaf.github.io/2023/04/07/%E4%BB%8E%E9%9B%B6%E5%BC%80%E5%A7%8B%E9%85%8D%E7%BD%AEWindows-vscode%E7%9A%84chisel%E7%8E%AF%E5%A2%83/)
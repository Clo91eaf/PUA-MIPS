简体中文 | [English](./README_EN.md)

NSCSCC 2023 二等奖 🏆

杭州电子科技大学 PUA 队 参赛作品

# 🚀 PUA (Powerful Ultimate Architecture) MIPS

[比赛总结 📖](https://clo91eaf.github.io/posts/nscscc2023/)

## 🛠️ 环境配置

本 Chisel 项目使用 sbt 进行构建.

- sbt 版本为 1.8.2

- scala 版本为 2.13.8

- Chisel 版本为 3.5.4

在 chisel 目录下执行以下命令生成 Verilog 文件:

```shell
make verilog
```

执行完成后，你将在 chisel/generated 目录下找到 PuaMips.v Verilog 文件，而 chisel/src/main/resources/mycpu_top.v 可作为顶层文件。

## 📚 简介

- 本项目在《CPU 设计实战》的基础上简单设计了一个高性能双发射六级流水 MIPS 处理器。初赛性能分 62，频率为 88MHz，IPC 为 1.25。
- 具体的设计情况请参阅 doc/final-design.pdf。
- 该处理器可以运行 linux6.5-rc3 最新版内核。
- 该处理器所有的代码不超过 5000 行，应该为龙芯杯历史上代码量相当小的处理器。
- 该处理器有较大的可改进空间，欢迎大家参考，由于时间紧张，没有对频率进行太大的优化。
- 本项目已进入尾声，不再进行维护.

## 📦 资源

1. [Git 提交消息约定](https://gitee.com/help/articles/4231#article-header0) 📜 - Git 的提交规范。
2. [Chisel-template](https://github.com/freechipsproject/chisel-template) 📁 - pua-mips 中的 Chisel 项目的初始化使用了这个模板。
3. [在线汇编器](https://godbolt.org/) 💻 - 在线的编译器，用来生成对应的 MIPS 二进制文件参考。
4. [MIPS 转换器](https://www.eg.bucknell.edu/~csci320/mips_web/) 🔄 - 负责把 MIPS 指令转换为二进制，并且提供对应指令的详细说明。
5. [Chisel 环境配置](https://clo91eaf.github.io/80b5fe4ebe03/) 🛠️

## 🧩 杂项

### 正确参考本项目的方式

1. ❌ 请不要直接复制本项目的代码。
2. ✍️ 在理解本项目模块的基础上，使用你自己的书写习惯进行代码实现，并尝试通过以下角度获得更好的代码实现:
   - 💬 代码的可读性。多写注释。
   - 🧰 代码的可维护性。解耦，模块化。本项目中除了少部分文件的代码量超过 200 行，其他文件的代码量都在 200 行以内。
   - 🚀 尽情释放 Chisel 语言的高度抽象能力。
   - 📝 撇弃软件思维，先设计，后实现。
3. 📊 通过性能计数器比较性能。永远是数据指导优化。
4. 📡 模拟器，仿真器。不要陷入波形的陷阱。不要安逸于现状。始终思考如何才能少看波形，如何获得更多的信息。

### 本项目的一些特点

你将在本项目中不可避免的看到：

1. 📝 中英文注释混杂。
2. 🎨 代码风格不统一。
3. 🤔 奇怪的命名。
4. 🧱 莫名其妙的硬编码设计。
5. 🔄 冗余的提交记录。

我们在开发中已经尽可能的规避这些问题，并且进行了多次重构。但是由于时间紧张，我们无法保证代码的完美。

如果我们的设计过程对你有启发，我们将感到很高兴。 🌟

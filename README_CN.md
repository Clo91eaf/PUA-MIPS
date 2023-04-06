# PUA(Powerful Ultimate Architecture)mips。

我们使用Codeup来管理我们的项目。

## 启动
你可以在项目目录下输入`make verilog`来生成verilog,verilog文件会自动生成在chisel/generated目录下。
你可以通过vivado/pua-mips.xpr启动项目并且运行对应的仿真以及生成比特流。

### vscode配置
- 有用的拓展: `Scala (Metals)` 、 `Scala Syntax (official)` 、 `Chisel Syntax`
  - `Scala (sbt)`与`Scala Language Server`这两个拓展同 `Scala (Metals)`拓展冲突,不建议安装.
  - 请在项目目录 `PUA-MIPS` 中禁用拓展(禁用(工作区)),在`chisel`目录中启用拓展.

## 资源:

1. [Git commit message conventions](https://gitee.com/help/articles/4231#article-header0)
git的提交规范。

2. Chisel was initialized using [Chisel-template](https://github.com/freechipsproject/chisel-template)。
pua-mips中的Chisel项目的初始化使用了这个模板。

3. [Online Assembler](https://godbolt.org/) 
在线的编译器,用来生成对应的mips二进制文件参考。

4. [MIPS Converter](https://www.eg.bucknell.edu/~csci320/mips_web/)
负责把mips指令转换为二进制,并且提供对应指令的详细说明。
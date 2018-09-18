# PoS printer libary
The goal of this library is to unify all the different PoS printer API's (and their quirks) under a single and simple API. Currently we aim for Etherner support.
#Installation
##Install using gradle
```
implementation 'com.initiative-soft:pos-printer-lib:0.2.0'
```
# How to use
```
//First create a printer instance of the required vendor.
val printer = PrinterFactory.createPrinterOfVendor(Vendor.SAM4S)
//Then connect to the printer
printer.connect("192.168.0.111")
//Then create a CommandBlock which will hold a list of all commands.
val commandBlock = printer.createCommandBlock()`
//Add the comand that you require.
commandBlock.addText("Hello world!")
commandBlock.addLineFeed(8)
commandBlock.addCut()
printer.sendCommandBlock(commandBlock)
```
![Plain](docs/images/plain.png)


The end result looks kinda of plain, but we can remedy this with the `TextFormat` class
```
val textFormat = printer.createTextFormat()
textFormat.setStyle(TextStyle.Underline, true)
textFormat.setStyle(TextStyle.Bold, true)
commandBlock.commandBlock.textFormat = textFormat
commandBlock.addText("Hello world, with style!")
commandBlock.addLineFeed(8)
commandBlock.addCut()
printer.sendCommandBlock(commandBlock)
```
![Styled](docs/images/styled.png)


And there we go, much better.





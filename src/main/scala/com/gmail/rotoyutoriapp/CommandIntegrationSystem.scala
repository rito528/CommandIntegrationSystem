package com.gmail.rotoyutoriapp

import org.bukkit.command.{Command, CommandSender}
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.{Bukkit, ChatColor}

import java.nio.file.Paths
import java.util

class CommandIntegrationSystem extends JavaPlugin with Listener {

  override def onEnable(): Unit = {
    super.onEnable()
    Bukkit.getServer.getPluginManager.registerEvents(this,this)
    getLogger.info("CommandIntegrationSystem enabled.")
  }

  override def onDisable(): Unit = {
    super.onDisable()
    getLogger.info("CommandIntegrationSystem disabled.")
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    super.onCommand(sender, command, label, args)
    val dataDir = "plugins/CommandIntegrationSystem/CommandLists/"
    if (label.equalsIgnoreCase("cis")) {
      if (!(args(0).equalsIgnoreCase("create") || args(0).equalsIgnoreCase("help") || args(0).equalsIgnoreCase("list")) ) {
        if (!File.isExistsFile(dataDir + args(1))) {
          sender.sendMessage(ChatColor.RED + "指定されたコマンドリストは存在しません！")
          return true
        }
      }
      if (args(0).equalsIgnoreCase("create") && sender.hasPermission("cis.create") && args.length == 2) {
        File.dirCheck(dataDir)
        if (File.isExistsFile(dataDir + args(1))) {
          sender.sendMessage(ChatColor.RED + "そのコマンドリストはすでに登録されています！")
          return true
        }
        File.createFile(dataDir + args(1))
        sender.sendMessage(ChatColor.AQUA + "コマンドリスト:" + args(1) + "を作成しました。")
        return true
      } else if (args(0).equalsIgnoreCase("delete") && sender.hasPermission("cis.delete") && args.length == 2) {
        File.deleteFile(dataDir + args(1))
        sender.sendMessage(ChatColor.AQUA + "コマンドリスト:" + args(1) + "を削除しました。")
        return true
      } else if (args(0).equalsIgnoreCase("addline") && sender.hasPermission("cis.addline")) {
        var command = ""
        var addLine = -1
        for (i <- 2 until args.length) {
          if (i != 2) {
            if (!args(i).contains("\"") && i == (args.length - 1)) addLine = if (args(i).toInt > 0) args(args.length - 1).toInt else 1
            else command += " " + args(i).replace("\"", "")
          } else command += args(i).replace("\"", "")
        }
        val data = File.readCommandFile(args(1))
        if (addLine > data.length) addLine = -1
        File.writeCommandFile(args(1), data, command, addLine)
        sender.sendMessage(ChatColor.AQUA + (if (addLine == -1) (data.length + 1).toString else addLine.toString) + "行目に" + command + "を追加しました！")
        return true
      } else if (args(0).equalsIgnoreCase("cat") && sender.hasPermission("cis.cat") && args.length == 2) {
        sender.sendMessage("コマンドリスト:" + args(1) + "を表示します...")
        sender.sendMessage("+---------------------------------------------------+")
        File.readCommandFile(args(1)).foreach(line => sender.sendMessage(ChatColor.AQUA + line))
        sender.sendMessage("+---------------------------------------------------+")
        return true
      } else if (args(0).equalsIgnoreCase("deleteline") && sender.hasPermission("cis.deleteline") && args.length == 3) {
        val data = File.readCommandFile(args(1))
        val deleteLine = if (args(2).toInt <= 0) 1 else if (args(2).toInt >= data.length) data.length else args(2).toInt
        File.deleteCommandFileLine(args(1), data, deleteLine)
        sender.sendMessage(s"${ChatColor.AQUA}コマンドリスト:${args(1)}の${deleteLine}行目を削除しました。")
        return true
      } else if (args(0).equalsIgnoreCase("list") && sender.hasPermission("cis.list") && args.length == 1) {
        sender.sendMessage("コマンドリスト一覧を表示します...")
        if (Paths.get(dataDir).toFile.listFiles().length == 0) {
          sender.sendMessage(ChatColor.AQUA + "作成されているコマンドリストはありません！")
          return true
        }
        Paths.get(dataDir).toFile.listFiles().foreach(name => sender.sendMessage("・" + name.getName))
        return true
      } else if (args(0).equalsIgnoreCase("start") && sender.hasPermission("cis.start") && (args.length == 2 || args.length == 3)) {
        var time = 0L
        if (args.length == 3) time = args(2).toLong
        sender.sendMessage(ChatColor.AQUA + time.toString + "秒後に、コマンドリスト:" + args(1) + "を実行します。")
        new BukkitRunnable {
          override def run(): Unit = {
            sender.sendMessage(ChatColor.AQUA + "コマンドリスト:" + args(1) + "を実行します...")
            File.readCommandFile(args(1)).foreach(line => Bukkit.dispatchCommand(Bukkit.getConsoleSender, line))
            sender.sendMessage(ChatColor.AQUA + "コマンドリストの実行が完了しました！")
          }
        }.runTaskLater(this, 20 * time)
        return true
      } else if (args(0).equalsIgnoreCase("help") && sender.hasPermission("cis.help") && args.length == 1) {
        sender.sendMessage("+---------------------------------------------------+")
        sender.sendMessage(ChatColor.AQUA + "/cis create [コマンドリスト名] - コマンドリストを作成します。")
        sender.sendMessage(ChatColor.AQUA + "/cis delete [コマンドリスト名] - コマンドリストを削除します。")
        sender.sendMessage(ChatColor.AQUA + "/cis addline [コマンドリスト名] [セットするコマンド(最初のスラッシュは入力しない、コマンドはダブルクォーテーションで囲ってください。)] [追加する行数(省略可能)] - コマンドリストにコマンドを追加します。")
        sender.sendMessage(ChatColor.AQUA + "/cis cat [コマンドリスト名] - コマンドリストに登録されているコマンド一覧を表示します。")
        sender.sendMessage(ChatColor.AQUA + "/cis deleteline [コマンドリスト名] [削除する行] - 指定したコマンドリストの指定した行のコマンドを削除します。")
        sender.sendMessage(ChatColor.AQUA + "/cis list - コマンドリスト一覧を表示します。")
        sender.sendMessage(ChatColor.AQUA + "/cis start [シナリオ名] [実行までの時間(秒)] - コマンドリストを実行します。")
        sender.sendMessage(ChatColor.AQUA + "/cis help - コマンド一覧を表示します。")
        sender.sendMessage("+---------------------------------------------------+")
        return true
      }
    }
    false
  }

  override def onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array[String]): util.List[String] = {
    super.onTabComplete(sender, command, alias, args)
    if (!command.getName.equalsIgnoreCase("cis")) return super.onTabComplete(sender,command,alias,args)
    if (args.length == 1) {
      return tabPermission(sender)
    }
    super.onTabComplete(sender,command,alias,args)
  }

  def tabPermission(sender: CommandSender): util.ArrayList[String] = {
    val permissionList = Map(
      "cis.create" -> "create",
      "cis.delete" -> "delete",
      "cis.addline" -> "addline",
      "cis.cat" -> "cat",
      "cis.deleteline" -> "deleteline",
      "cis.list" -> "list",
      "cis.start" -> "start",
      "cis.help" -> "help"
    )
    val commandList = new util.ArrayList[String]()
    permissionList.foreach({case (p,cmd) =>
      if (sender.hasPermission(p)) commandList.add(cmd)
    })
    commandList
  }

}

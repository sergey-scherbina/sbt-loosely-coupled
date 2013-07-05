sbt-loosely-coupled
===================

SBT plugin for loosely coupled projects.

##Motivation

Most used pattern in advanced enterprise projects is the "Loosely coupling". It means if project consists of number of units which don't reference between themselves in way other as just libraries dependency, the same as any other third-party libraries are referenced. That is very awesome and flexible approatch to design units in project and it has many advantages such as true independence and abstraction. But in the same time it causes big difficulties in building and even development in IDE using refactorings etc. You can't just build the loosely coupled project. Instead you must build units indepentently and do it in proper order. Often those units even are in different git repositories, so they can't just know where could be found their siblings which are required for compilation of particular unit. They just reference them as some library dependencies but instead fetch from maven central repo they are fetched from local repo where they should be installed before. In practice it leads to headache a lot. In addition, you can't just open whole project in your favourite IDE like as JetBrain's IDEA and refactoring some class across all project. No, insted you have to open each unit as separate project and carefully make the same changes in each. So it does feel so bad if you need to work with loosely coupled projects in big enough team where you just can't be sure is success of simple compilation of project. This plugin does solution of almost all of the troubles with true loosely coupled projects build using SBT.

##Sample

I'm not managed yet to write any really good dicumentation on this plugin, but there is just abstract testing sample which could be used to get idea how the plugin should be used and it can be found in 'sample' subfolder of this project. The sample represent three units 'foo', 'bar' and 'app' where 'app' unit is the simple 'def main { println(bar) }' which uses method from 'bar' unit, and 'bar' unit just uses 'foo' as some dependency to get simple data to return. They are referenced as libraryDependencies+= in their build.sbt files. Also there is 'plugin' unit which is used in all three units as plugin dependency to share some common settings in project, which is very good and recommended practice in SBT projects. As result when you will try to build that simple but loosely couple project then you have to in first go to 'plugin' unit folder, run there 'sbt publish-local', then go to 'foo' unit subfolder, run there 'sbt publish-local' and repeat it for all rest units 'bar' and 'app'. Or you just cant compile and run 'app' if don't go through all those build steps. And you should repeat it if you or somebody in your team change something in 'plugin' or 'foo' or 'bar'.

You can try simple solution for that problem - just clone from git each unit into one working folder and resolve depencencies usign known relative paths. But it requires to change the units build definitions from library dependencies to direct referencec between projects. First bad thing in this case is that it is need to change project but it isn't most bad. More worse is the second thing - you can't build modules independently anymore so you can't push those changes in builds into git and you have to mess with branching and merging. You could if you like but I'm not :)

More awesome solution could be to define other build which just refer all units. You could see it in 'loosely' subfolder. It uses plain 'vanilla' SBT feature to depend between builds. I like that feature a lot. But unfortunately it doesn't work in that case - build just cant be even opened by SBT if you didn't before publish local of plugin and units. And if you will try to rebuild the project then you realize that you must do repeated publish-local's before sucessfully rebuild faced mess of compilation issues in process. It isn't fun.

So just using that plugin you could be completely happy with those loosely coupled projects. Look to 'coupled' subfolder to see sample of improved a bit build which simply build the project and you even could run once 'gen-idea no-sbt-build-module' command (if you have 'sbt-idea' referenced in your ~/.sbt/plugins/build.sbt) for import to IDEA all units as whole project. Of course, before you must do once 'sbt publish-local' in root folder of 'sbt-loosely-coupled' to install the plugin. So far I haven't managed yet to publish 'sbt-loosely-coupled' plugin somewhere in public repository. But I hope it will to be someday :)


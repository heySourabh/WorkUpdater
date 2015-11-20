# WorkUpdater

[Installer](https://github.com/heySourabh/WorkUpdater/releases/download/1.0/Installer.jar)

[Uninstaller](https://github.com/heySourabh/WorkUpdater/releases/download/1.0/Uninstaller.jar)

[Source Code](https://github.com/heySourabh/WorkUpdater/archive/1.0.zip)

A Java tool which periodically pops up a simple dialog box for saving your work updates. This tool may be used for keeping track of the activities which you are doing and the amount of time spent in the same. Thus, it can be used for inceasing your work efficiency. In some organizations the employees have to maintain a weekly log of activities, in such a scenario also you may use this tool so that you do not forget the work done (This is the reason for the inception of this tool in the first place).

## Notes:
- After packaging the class files, remember to add the PeriodicWorkUpdate.jar into the Installer using either jar command or using a archive manager like winzip.
- The installer and uninstaller are designed to work only on windows, as it uses reg command to setup the registry.
- The PeriodicWorkUpdater mostly will work in all environments, except for the fact that it tries to use notepad for viewing the updates. The java.awt.Desktop class may be used in future for a platform independent behaviour.

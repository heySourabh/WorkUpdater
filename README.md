# WorkUpdater
A Java tool which periodically pops up a simple dialog box for updating your work updates.

## Notes:
- After packaging the class files, remember to add the PeriodicWorkUpdate.jar into the Installer using either jar command or using a archive manager like winzip.
- The installer and uninstaller are designed to work only on windows, as it uses reg command to setup the registry.
- The PeriodicWorkUpdater mostly will work in all environments, except for the fact that it tries to use notepad for viewing the updates. The java.awt.Desktop class may be used in future for a platform independent behaviour.

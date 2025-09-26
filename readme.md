# TRA Re-referencer 

<a name="a_mod_overview"></a>
### Overview
<p>
This tool corrects the party character and NPC string references in a .GAM file after an action (e.g. mod or game re-install) that causes a change to dialog.tlk. It does so by comparing dialog.tlk pre-change to dialog.tlk post change, and correcting string references based on the text strings themselves.

Usage:
1. Obtain the "old" dialog.tlk file. You can do this by executing the following command in the folder containing BG1EE, BG2EE, or SoD.
	./weidu.exe --traify-tlk > old.tlk
2. Make whatever mod changes or game reinstalls you were planning.
3. Obtain the "new" dialog.tlk file. You can do this by executing the following command in the folder containing BG1EE, BG2EE, or SoD.
	./weidu.exe --traify-tlk > new.tlk
4. Run the provided .jar file, and provide the paths to the old.tlk file, the new .tlk file, and the .GAM file that you want to update. You can also pass in a parameters file that includes the three paths.<br>
	Example params.in<br>
	[absolute path to old.tlk]<br>
	[absolute path to new.tlk]<br>
	[absolute path to the .GAM file associated with the save game that you need to update]<br>
	
	java -jar "F:\repo\externaljars\TRARereferencer.jar" params.in
</p>
<p>
For support, contact support@fosiemods.net.
</p>
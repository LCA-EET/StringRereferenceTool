$basePath = "TRARereferencer"
$archive = $basePath + ".zip"
Remove-Item -LiteralPath $modPath -Force -Recurse
New-Item -Path $basePath -ItemType Directory
Copy-Item -Path ("F:\repo\externaljars\TRARereferencer.jar") -Destination $basePath
Copy-Item -Path ("params.in") -Destination $basePath
Copy-Item -Path ("readme.md") -Destination $basePath
Copy-Item -Path ("release_notes.md") -Destination $basePath

$7zipPath = "$env:ProgramFiles/7-Zip/7z.exe"

if (-not (Test-Path -Path $7zipPath -PathType Leaf)) {
	$7zipPath = "F:/Program Files/7-Zip/7z.exe"
}

Set-Alias Start-SevenZip $7zipPath

$Source = "./" + $basePath + "/*"
$Target = "./" + $archive

Start-SevenZip a -mx=9 $Target $Source

Remove-Item -LiteralPath $basePath -Force -Recurse
Get-FileHash $archive -Algorithm SHA256 > SHA256.txt

Copy-Item -Path $archive -Destination ("\\nas.home.lan\smbuser\Home\Installers\" + $archive)
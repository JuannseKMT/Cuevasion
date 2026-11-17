param([int]$Days = 30)
$secret   = "CV1x7Qz9Lm2Rt5Yb8Nw3Kd6Pf4Hs0Gj"
$alphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
$epoch    = [datetime]::new(2026,1,1)
$day = [long](([datetime]::UtcNow.AddDays($Days) - $epoch).TotalDays)
$sb = New-Object System.Text.StringBuilder
$v = $day
for ($i=0; $i -lt 4; $i++) { $sb.Insert(0, $alphabet[[int]($v -band 31)]); $v = $v -shr 5 }
$payload = $sb.ToString()
$h = New-Object System.Security.Cryptography.HMACSHA256
$h.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$raw = $h.ComputeHash([Text.Encoding]::UTF8.GetBytes($payload))
$sig = -join ($raw | ForEach-Object { $alphabet[$_ % 32] })
"CV-$payload-$($sig.Substring(0,5))-$($sig.Substring(5,5))"
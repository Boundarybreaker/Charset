#!/bin/sh
TMPDIR=$(mktemp -d /tmp/charset.XXXXXXXX)
OUTDIR=$(pwd)
mkdir -p releases/"$1"/
mkdir -p $TMPDIR/in

cd $TMPDIR/in
unzip "$OUTDIR"/build/libs/Charset-"$1".jar
mv mcmod.info mcmod-orig.info

for i in `jq -r '.modules | keys[]' "$OUTDIR"/modules.json`; do
	echo "[" > mcmod.info
	jq '.modList[] | select((.modid | ascii_downcase) == "charset'$i'")' mcmod-orig.info >> mcmod.info
	echo "]" >> mcmod.info
	if [ "$i" = "lib" ]; then
		jar cvf "$OUTDIR"/releases/"$1"/charset-"$1"-api.jar \
			pl/asie/charset/api LICENSE-2.0 NOTICE
		jar cvf "$OUTDIR"/releases/"$1"/charset-"$1"-"$i".jar \
			assets/charset"$i" \
			pl/asie/charset/"$i" pl/asie/charset/api \
			pack.mcmeta mcmod.info LICENSE* NOTICE
	else
		jar cvf "$OUTDIR"/releases/"$1"/charset-"$1"-"$i".jar \
			assets/charset"$i" \
			pl/asie/charset/"$i" \
			pack.mcmeta mcmod.info LICENSE* NOTICE
	fi
	rm mcmod.info
done

cd "$OUTDIR"
cp modules.json releases/"$1"/
if [ -f changelog/"$1".txt ]; then
  cp changelog/"$1".txt releases/"$1"/changelog.txt
fi
rm -rf $TMPDIR

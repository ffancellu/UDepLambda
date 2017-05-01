find $1 -print0 | while IFS= read -r -d '' file
do
    if [ -f gold.amr ]
    echo $file
    then
        timeout 120s python amr-evaluation/smatch/smatch.py -f $file/gold.amr $file/silver.amr > $file/smatch_score.txt
    fi
done

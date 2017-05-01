CANDC=/fs/saga0/federico/git/learningbyreading/ext/candc/

find $1 -print0 | while IFS= read -r -d '' file
do 
    if [[ "$file" == *en.tok.par ]];
    then
        echo $file
        #$CANDC"bin/candc" --input $file --models $CANDC"models/boxer" --candc-printer boxer > $(dirname "${file}")"/en.test.ccg"
        $CANDC"bin/boxer" --input $(dirname "${file}")"/en.test.ccg" --box false --semantics drs --integrate --warnings --resolve --tense --instantiate --modal --theory sdrt --copula false --nn --format xml --output $(dirname "${file}")"/en.silver.drs.xml"
    fi
done

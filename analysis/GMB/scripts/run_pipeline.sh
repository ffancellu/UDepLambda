echo "Tokenizing inputs"
python get_tokenized.py $1
echo "Parsing inputs into DRGs"
./drg_parse.sh $1
echo "Generating AMR-like representations for gold and silver DRGs"
python compare_DRGs.py $1
echo "Calculating SMATCH scores"

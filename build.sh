echo "cleaning previously built files..."
rm -rf "./bin"
mkdir "./bin"

echo ""

echo "building MarkovBot 98..."
javac -d "./bin" -g -cp "./src" src/com/jboby93/markovbot/*.java

echo "done!"

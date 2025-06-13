#!/bin/bash

# 配置区
PROJECT_DIR="$(pwd)"
OUTPUT_NAME="soft_doc"
MAX_PAGES=60

# 安装必要工具
if ! command -v pandoc &> /dev/null; then
  sudo apt install -y pandoc texlive-xetex python3-pygments
fi

# 生成Markdown
echo "# 软件著作权源程序文档" > ${OUTPUT_NAME}.md
echo "**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')" >> ${OUTPUT_NAME}.md
echo "" >> ${OUTPUT_NAME}.md

# 排除mapper、test、model、annotation、common、config、constant、exception和service目录
find src/main/java/ -name "*.java" ! -path "*/test/*" ! -path "*/mapper/*" ! -path "*/model/*" ! -path "*/annotation/*" ! -path "*/common/*" ! -path "*/config/*" ! -path "*/constant/*" ! -path "*/exception/*" ! -path "*/controller/*" | while read file; do
  echo "## 文件: ${file##*/}" >> ${OUTPUT_NAME}.md
  echo "**路径**: \`${file}\`" >> ${OUTPUT_NAME}.md
  echo "\`\`\`java" >> ${OUTPUT_NAME}.md
  sed 's/\(password\|key\|secret\)\s*=\s*".*"/\1 = "<REMOVED>"/g' "$file" >> ${OUTPUT_NAME}.md
  echo "\`\`\`" >> ${OUTPUT_NAME}.md
  echo "" >> ${OUTPUT_NAME}.md
done

# 转换为Word
pandoc ${OUTPUT_NAME}.md -o ${OUTPUT_NAME}.docx \
  --highlight-style pygments \
  --table-of-contents \
  --toc-depth=2 \
  --resource-path=$PROJECT_DIR

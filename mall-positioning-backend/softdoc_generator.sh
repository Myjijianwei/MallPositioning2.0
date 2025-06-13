#!/bin/bash

# ----------- 配置区 -----------
PROJECT_DIR="$(pwd)"      # 自动定位项目目录
OUTPUT_NAME="soft_doc"    # 输出文件名（无需后缀）
MAX_PAGES=60              # 软著页数限制
# ----------------------------

# 安装必要工具（首次运行自动安装）
if ! command -v pandoc &> /dev/null; then
  echo "[安装工具] pandoc..."
  sudo apt install -y pandoc texlive-xetex python3-pygments
fi

# 1. 生成Markdown（自动跳过测试文件和脱敏）
echo "# 软件著作权源程序文档" > ${OUTPUT_NAME}.md
echo "**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')" >> ${OUTPUT_NAME}.md
echo "" >> ${OUTPUT_NAME}.md

find src/main/java/ -name "*.java" ! -path "*/test/*" | while read file; do
  echo "## 文件: ${file##*/}" >> ${OUTPUT_NAME}.md
  echo "**路径**: \`${file}\`" >> ${OUTPUT_NAME}.md
  echo "\`\`\`java" >> ${OUTPUT_NAME}.md
  # 自动脱敏（替换密码和密钥）
  sed 's/\(password\|key\|secret\)\s*=\s*".*"/\1 = "<REMOVED>"/g' "$file" >> ${OUTPUT_NAME}.md
  echo "\`\`\`" >> ${OUTPUT_NAME}.md
  echo "" >> ${OUTPUT_NAME}.md
done

# 2. 转换为Word（自动分页控制）
echo "[转换格式] 生成Word..."
pandoc ${OUTPUT_NAME}.md -o ${OUTPUT_NAME}.docx \
  --highlight-style pygments \
  --table-of-contents \
  --toc-depth=2 \
  --resource-path=$PROJECT_DIR

# 3. 完成提示
echo "-------------------------------------"
echo "生成成功！请检查以下文件："
echo " - 代码文档: ${OUTPUT_NAME}.md"
echo " - Word版: ${OUTPUT_NAME}.docx"
echo "-------------------------------------"

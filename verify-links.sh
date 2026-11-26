#!/usr/bin/env bash
# Проверяет все ссылки `mvn -Dtest=<класс>#<метод>` в tasks/*/TASK-*.md:
#  1) тестовый класс существует (файл с нужным package и именем класса);
#  2) @Test-метод существует в этом файле.
# Выводит сводку; ненулевой код возврата, если есть ошибки.
set -u
cd "$(dirname "$0")"

# Карта "tNN.ClassName -> содержимое файла" (один проход, без подпроцессов).
declare -A CONTENT=()
while IFS= read -r f; do
  base=${f##*/}; base=${base%.java}
  content=$(cat "$f")
  firstline=${content%%$'\n'*}
  case "$firstline" in
    "package com.example.jpalab.tasks.t"*";")
      pkg=${firstline#package com.example.jpalab.tasks.}
      pkg=${pkg%;}
      CONTENT["$pkg.$base"]=$content
      ;;
  esac
done < <(find src/test/java/com/example/jpalab/tasks -name "*Test.java")

errors=0; total=0
while IFS= read -r md; do
  while IFS= read -r fq; do
    [ -z "$fq" ] && continue
    total=$((total+1))
    cls=${fq%#*}
    method=${fq#*#}
    key=${cls#com.example.jpalab.tasks.}
    body=${CONTENT[$key]:-}
    if [ -z "$body" ]; then
      echo "BAD (класс не найден): $fq [$md]"
      errors=$((errors+1))
    elif ! [[ $body =~ void[[:space:]]+$method[[:space:]]*\(\) ]]; then
      echo "BAD (метод не найден): $fq [$md]"
      errors=$((errors+1))
    fi
  done < <(grep -oE "mvn -Dtest=com\.example\.jpalab\.tasks\.t[0-9]+\.[A-Za-z0-9]+#[A-Za-z0-9_]+" "$md" | sed 's/mvn -Dtest=//')
done < <(find tasks -name "TASK-*.md" | sort)
echo "Проверено ссылок: $total, ошибок: $errors"
[ "$errors" -eq 0 ]

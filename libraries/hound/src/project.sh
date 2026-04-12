#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Массив с исключаемыми директориями
EXCLUDE_DIRS=("knowage_base" "generated" "generated_code" "examples")

# Функция для проверки, нужно ли исключить директорию
is_excluded_dir() {
    local dir_name="$1"
    for excluded in "${EXCLUDE_DIRS[@]}"; do
        if [[ "$dir_name" == "$excluded" ]]; then
            return 0  # true - нужно исключить
        fi
    done
    return 1  # false - не нужно исключать
}

# Переменные для вывода
OUTPUT_FILE=""
APPEND_MODE=false

# Функция для вывода (в консоль или в файл)
print_output() {
    local text="$1"
    local color="$2"
    
    # Если указан цвет и вывод идет в консоль (не в файл)
    if [[ -n "$color" && -z "$OUTPUT_FILE" ]]; then
        echo -e "${color}${text}${NC}"
    else
        # Для файла выводим без цветов
        echo "$text"
    fi
    
    # Если указан файл для вывода, сохраняем туда (без цветов)
    if [[ -n "$OUTPUT_FILE" ]]; then
        if [[ "$APPEND_MODE" == true ]]; then
            echo "$text" >> "$OUTPUT_FILE"
        else
            echo "$text" > "$OUTPUT_FILE"
            APPEND_MODE=true  # После первой записи переключаемся в режим добавления
        fi
    fi
}

# Функция для вывода дерева
print_tree() {
    local dir=$1
    local prefix=$2
    local files=("$dir"/*)
    local last_index=$((${#files[@]} - 1))
    
    # Добавляем скрытые файлы если нужно
    if [[ "$SHOW_HIDDEN" == "true" ]]; then
        local hidden_files=("$dir"/.*)
        if [[ ${#hidden_files[@]} -gt 2 ]]; then  # >2 потому что всегда есть . и ..
            files=("${files[@]}" "${hidden_files[@]}")
            # Пересортируем и пересчитываем последний индекс
            IFS=$'\n' files=($(printf "%s\n" "${files[@]}" | sort))
            last_index=$((${#files[@]} - 1))
        fi
    fi
    
    # Фильтруем файлы
    local filtered_files=()
    for file in "${files[@]}"; do
        local basename=$(basename "$file")
        
        # Пропускаем . и ..
        if [[ "$basename" == "." || "$basename" == ".." ]]; then
            continue
        fi
        
        # Пропускаем скрытые файлы если не указано обратное
        if [[ "$basename" == .* ]] && [[ "$SHOW_HIDDEN" != "true" ]]; then
            continue
        fi
        
        # Проверяем на исключение директорий
        if [[ -d "$file" ]] && is_excluded_dir "$basename"; then
            print_output "${prefix}├── ${basename}/ (исключено)" "$YELLOW"
            continue
        fi
        
        # Проверяем на исключение .pyc файлов
        if [[ -f "$file" ]] && [[ "$basename" == *.pyc ]]; then
            continue  # Просто пропускаем .pyc файлы
        fi
        
        filtered_files+=("$file")
    done
    
    # Обновляем список файлов и последний индекс
    files=("${filtered_files[@]}")
    last_index=$((${#files[@]} - 1))
    
    for i in "${!files[@]}"; do
        local file="${files[$i]}"
        local basename=$(basename "$file")
        
        # Определяем символ для текущего элемента
        if [[ $i -eq $last_index ]]; then
            print_output "${prefix}└── ${basename}" "$BLUE"
            new_prefix="${prefix}    "
        else
            print_output "${prefix}├── ${basename}" "$BLUE"
            new_prefix="${prefix}│   "
        fi
        
        # Если это директория, рекурсивно выводим её содержимое
        if [[ -d "$file" ]]; then
            print_tree "$file" "$new_prefix"
        fi
    done
}

# Функция для отображения справки
show_help() {
    echo "Использование: $0 [опции] [директория]"
    echo ""
    echo "Опции:"
    echo "  -a, --all                 Показывать скрытые файлы"
    echo "  -o, --output FILE         Сохранить результат в файл"
    echo "  -f, --force               Перезаписать файл если существует (по умолчанию)"
    echo "  --append                  Добавить в конец файла если существует"
    echo "  -h, --help                Показать эту справку"
    echo ""
    echo "Исключаемые директории: knowage_base, generated, generated_code, examples"
    echo "Исключаемые файлы: *.pyc"
    echo ""
    echo "Примеры:"
    echo "  $0                        # Текущая директория в консоль"
    echo "  $0 -o tree.txt             # Сохранить в файл"
    echo "  $0 -a -o tree.txt /home    # Скрытые файлы в /home сохранить в файл"
    echo "  $0 --append -o tree.txt    # Добавить в существующий файл"
    exit 0
}

# Парсинг аргументов
SHOW_HIDDEN="false"
START_DIR="."
FORCE_MODE=true  # По умолчанию перезаписываем

while [[ $# -gt 0 ]]; do
    case $1 in
        -a|--all)
            SHOW_HIDDEN="true"
            shift
            ;;
        -o|--output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        -f|--force)
            FORCE_MODE=true
            shift
            ;;
        --append)
            FORCE_MODE=false
            shift
            ;;
        -h|--help)
            show_help
            ;;
        *)
            START_DIR="$1"
            shift
            ;;
    esac
done

# Проверяем существование директории
if [[ ! -d "$START_DIR" ]]; then
    print_output "Ошибка: Директория '$START_DIR' не найдена" "$RED"
    exit 1
fi

# Проверяем файл вывода
if [[ -n "$OUTPUT_FILE" ]]; then
    # Создаем директорию для файла если нужно
    OUTPUT_DIR=$(dirname "$OUTPUT_FILE")
    if [[ ! -d "$OUTPUT_DIR" ]]; then
        mkdir -p "$OUTPUT_DIR" 2>/dev/null
        if [[ $? -ne 0 ]]; then
            print_output "Ошибка: Не удалось создать директорию '$OUTPUT_DIR'" "$RED"
            exit 1
        fi
    fi
    
    # Проверяем можно ли писать в файл
    if [[ -f "$OUTPUT_FILE" ]]; then
        if [[ "$FORCE_MODE" == true ]]; then
            # Перезаписываем файл
            > "$OUTPUT_FILE" 2>/dev/null
            if [[ $? -ne 0 ]]; then
                print_output "Ошибка: Нет прав на запись в '$OUTPUT_FILE'" "$RED"
                exit 1
            fi
            print_output "Файл '$OUTPUT_FILE' будет перезаписан" "$YELLOW"
        else
            # Проверяем можно ли добавить в файл
            if [[ ! -w "$OUTPUT_FILE" ]]; then
                print_output "Ошибка: Нет прав на запись в '$OUTPUT_FILE'" "$RED"
                exit 1
            fi
            print_output "Данные будут добавлены в конец файла '$OUTPUT_FILE'" "$YELLOW"
        fi
    else
        # Проверяем можно ли создать файл
        touch "$OUTPUT_FILE" 2>/dev/null
        if [[ $? -ne 0 ]]; then
            print_output "Ошибка: Не удалось создать файл '$OUTPUT_FILE'" "$RED"
            exit 1
        fi
        rm "$OUTPUT_FILE"  # Удаляем временный файл
    fi
fi

# Выводим информацию о начале сканирования
print_output "\nСканирование директории: $START_DIR" "$GREEN"
print_output "$(date '+%Y-%m-%d %H:%M:%S')" "$YELLOW"
print_output "Исключаемые элементы:"
print_output "  - Директории: knowage_base, generated, generated_code, examples" "$YELLOW"
print_output "  - Файлы: *.pyc" "$YELLOW"
print_output ""

# Выводим корневую директорию
print_output "$(basename "$START_DIR")/" "$GREEN"

# Запускаем вывод дерева
print_tree "$START_DIR" ""

# Финальное сообщение
if [[ -n "$OUTPUT_FILE" ]]; then
    print_output "\nРезультат сохранен в файл: $OUTPUT_FILE" "$GREEN"
    
    # Показываем размер файла
    if [[ -f "$OUTPUT_FILE" ]]; then
        FILE_SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)
        print_output "Размер файла: $FILE_SIZE" "$YELLOW"
    fi
fi

print_output "Готово!" "$GREEN"

# Ожидание нажатия клавиши перед закрытием
echo ""
echo -e "${YELLOW}Нажмите Enter для закрытия окна...${NC}"
read
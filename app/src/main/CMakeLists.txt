cmake_minimum_required(VERSION 3.10)

# Название проекта и используемые языки
project(RegisterActivityNative LANGUAGES C)

# Установка минимальной версии Android
set(CMAKE_SYSTEM_NAME Android)
set(CMAKE_SYSTEM_VERSION 24)
set(ANDROID_PLATFORM android-24)

# Указание архитектуры (замените на необходимую вам)
set(ANDROID_ABI "x86")
set(CMAKE_ANDROID_ARCH_ABI "x86")

# Указание пути к NDK
set(ANDROID_NDK "C:/Users/sashk/AppData/Local/Android/Sdk/ndk/28.0.12433566")
set(CMAKE_ANDROID_NDK "${ANDROID_NDK}")

# Указание пути к инструментарию Android NDK
set(CMAKE_TOOLCHAIN_FILE "${ANDROID_NDK}/build/cmake/android.toolchain.cmake")

# Определяем тип сборки (Debug или Release)
set(CMAKE_BUILD_TYPE Debug)

# Устанавливаем пути для выходных файлов
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin")

# Добавляем исходные файлы
add_library(native-lib SHARED cpp/native-lib.c)

# Указываем, что проект будет использовать JNI
find_library(log-lib log)

# Линкуем библиотеку log для Android
target_link_libraries(native-lib ${log-lib})

cmake_minimum_required(VERSION 3.22.1)

project("colorchecker")

# OpenCV
set(OpenCV_STATIC ON)
set(OpenCV_DIR $ENV{OPENCV_ANDROID}/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Colorlib
add_library(colorlib STATIC IMPORTED)
set_target_properties(colorlib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/colorlib/libs/${ANDROID_ABI}/libcolorlib.a)
include_directories(${CMAKE_SOURCE_DIR}/colorlib/include)
include_directories(${CMAKE_SOURCE_DIR}/colorlib/libs/${ANDROID_ABI}/include)

add_library(
        colorchecker

        SHARED
        native-lib.cpp)

find_library(log-lib log)

find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
        colorchecker
        colorlib

        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        ${log-lib})
cmake_minimum_required(VERSION 3.12)
project(ultreonjv LANGUAGES CXX)

file(GLOB_RECURSE HEADER_FILES "include/**/*.cpp")
add_library(ultreonjv SHARED ${HEADER_FILES}
        include/e.cpp)

set(CMAKE_CXX_STANDARD 21)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

target_include_directories(ultreonjv
        PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>
)

set_target_properties(ultreonjv PROPERTIES
        VERSION 1.0.0
        SOVERSION 1
)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

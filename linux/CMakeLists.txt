cmake_minimum_required(VERSION 3.19)

project(spade_ace VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Include the native library
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../native/cpp spade_ace_native)

# Linux-specific configurations
if(UNIX AND NOT APPLE)
    # Find required libraries
    find_package(PkgConfig REQUIRED)
    
    # Set RPATH for shared libraries
    set_target_properties(spade_ace_native PROPERTIES
        INSTALL_RPATH "$ORIGIN"
        BUILD_WITH_INSTALL_RPATH TRUE
    )
endif()
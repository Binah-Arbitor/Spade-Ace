cmake_minimum_required(VERSION 3.16)
project(spade_ace_native)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_package(PkgConfig REQUIRED)

# Try to find Crypto++ library
find_path(CRYPTOPP_INCLUDE_DIR cryptopp/cryptlib.h 
    PATHS /usr/include /usr/local/include
    PATH_SUFFIXES cryptopp crypto++)
    
find_library(CRYPTOPP_LIBRARY 
    NAMES cryptopp crypto++ 
    PATHS /usr/lib /usr/local/lib
    PATH_SUFFIXES cryptopp)

if(NOT CRYPTOPP_INCLUDE_DIR OR NOT CRYPTOPP_LIBRARY)
    message(STATUS "Crypto++ not found, will build from source")
    
    # Download and build Crypto++
    include(ExternalProject)
    ExternalProject_Add(
        cryptopp_external
        URL https://www.cryptopp.com/cryptopp890.zip
        URL_HASH SHA256=bdb4ac2b9c1f0d3a7b2e1c4b26a6b4a5c5f7b6b7e9c3d4a5b1c2d3e4f5a6b7c8
        CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/cryptopp
                   -DBUILD_SHARED=OFF
                   -DBUILD_TESTING=OFF
                   -DCMAKE_BUILD_TYPE=Release
        INSTALL_DIR ${CMAKE_BINARY_DIR}/cryptopp
    )
    
    set(CRYPTOPP_INCLUDE_DIR ${CMAKE_BINARY_DIR}/cryptopp/include)
    set(CRYPTOPP_LIBRARY ${CMAKE_BINARY_DIR}/cryptopp/lib/libcryptopp.a)
    add_dependencies(spade_ace_native cryptopp_external)
endif()

# Create the native library
add_library(spade_ace_native SHARED
    src/crypto_engine.cpp
    src/file_analyzer.cpp
    src/attack_methods.cpp
    src/performance_manager.cpp
    src/ffi_interface.cpp
    src/gpu_engine.cpp
)

target_include_directories(spade_ace_native PRIVATE
    include
    ${CRYPTOPP_INCLUDE_DIR}
)

target_link_libraries(spade_ace_native
    ${CRYPTOPP_LIBRARY}
    pthread
)

# Set output directory based on platform
if(WIN32)
    set_target_properties(spade_ace_native PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../windows/
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../windows/)
elseif(APPLE)
    set_target_properties(spade_ace_native PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../macos/)
else()
    set_target_properties(spade_ace_native PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../linux/)
endif()
cmake_minimum_required(VERSION 3.16.0)

project(SpadeAce VERSION 1.0.0)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Qt6 components
find_package(Qt6 REQUIRED COMPONENTS Core Widgets)

# Auto-generate MOC files
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

# Include directories
include_directories(src)

# Source files
set(SOURCES
    src/main.cpp
    src/MainWindow.cpp
    src/DecryptionWindow.cpp
    src/FileOperationsWindow.cpp
    src/SettingsWindow.cpp
    src/core/DecryptionEngine.cpp
    src/core/CryptoUtils.cpp
    src/data/Models.cpp
)

# Header files
set(HEADERS
    src/MainWindow.h
    src/DecryptionWindow.h
    src/FileOperationsWindow.h
    src/SettingsWindow.h
    src/core/DecryptionEngine.h
    src/core/CryptoUtils.h
    src/data/Models.h
)

# Create executable
add_executable(SpadeAce ${SOURCES} ${HEADERS})

# Link Qt libraries
target_link_libraries(SpadeAce Qt6::Core Qt6::Widgets)

# Find and link OpenSSL for encryption
find_package(OpenSSL REQUIRED)
target_link_libraries(SpadeAce OpenSSL::SSL OpenSSL::Crypto)

# Set application properties
set_target_properties(SpadeAce PROPERTIES
    OUTPUT_NAME "SpadeAce"
    WIN32_EXECUTABLE TRUE
)

# Install targets
install(TARGETS SpadeAce
    BUNDLE DESTINATION .
    RUNTIME DESTINATION bin
)
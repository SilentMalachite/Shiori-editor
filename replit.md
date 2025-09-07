# Overview

This repository contains a comprehensive JavaFX-based Markdown-compatible Japanese text editor with advanced features. The project is a professional desktop text editor specifically designed for Markdown files with complete Japanese language support, featuring syntax highlighting, paragraph folding, theme switching, and programming language support. The application uses JavaFX 17+ as its primary UI framework and includes RichTextFX for advanced text editing capabilities.

# User Preferences

Preferred communication style: Simple, everyday language.
UI Language: Japanese
Font Preferences: Japanese fonts optimized (Yu Gothic UI, Meiryo)
Theme: Automatic OS theme detection with manual toggle support

# System Architecture

## Desktop Application Framework
The application is built on JavaFX 17+ with Maven as the build system. JavaFX provides the foundation for creating a responsive desktop text editor with native look and feel across Windows, macOS, and Linux platforms. The architecture follows a modular design pattern with separate components for different functionalities.

## Core Components

### Text Editor Engine (TextEditor.java)
Main application class that orchestrates all components and provides the primary user interface with menu bar, toolbar, and text editing area.

### Advanced Text Editing (RichTextFX Integration)
Uses RichTextFX CodeArea for advanced text editing capabilities including:
- Line numbers display
- Real-time syntax highlighting
- Rich text formatting support
- Japanese text input optimization

### Syntax Highlighting System
Multi-language syntax highlighting implementation supporting:

- **Markdown Highlighting (MarkdownHighlighter.java)**: Hierarchical headings (H1-H6), text formatting (bold, italic, strikethrough), code blocks, quotes, links, and lists
- **Programming Language Support (ProgrammingLanguageSupport.java)**: Java, Go, C/C++, and Haskell with language-specific keyword, string, comment, and number highlighting

### Paragraph Folding System (ParagraphFoldingManager.java)
Advanced paragraph management with:
- Ctrl + Enter + Mouse Click folding/expanding
- Intelligent paragraph boundary detection
- Visual folding indicators
- Support for heading-based and list-based folding

### Theme Management (ThemeManager.java)
Dual-theme system with:
- **Light Theme ("Light Ja")**: Professional white background with Japanese font optimization
- **Dark Theme**: Modern dark background for low-light environments
- OS theme auto-detection
- Runtime theme switching capability

## Japanese Language Support
Complete UTF-8 encoding support with:
- Japanese font prioritization (Yu Gothic UI, Meiryo, MS Gothic)
- Proper text rendering for mixed Japanese-English content
- IME-compatible text input
- Japanese-localized UI elements

## Color Scheme and Typography
- **Light Theme**: Professional blue headings (#2E74B5), dark gray text (#323130), orange code highlighting (#e36209)
- **Dark Theme**: Cyan headings (#4FC3F7), light gray text (#f0f0f0), red code highlighting (#f97583)
- **Programming Languages**: VS Code-inspired color scheme with distinct colors for keywords, strings, comments, and numbers

## Performance Optimization
- Debounced syntax highlighting (500ms delay) to prevent performance issues during rapid typing
- Efficient regular expression-based pattern matching
- Lazy loading of language-specific highlighters

# External Dependencies

## JavaFX Runtime Environment
- **JavaFX SDK 18.0.2**: Core UI framework providing windowing, controls, and CSS styling capabilities
- **JavaFX Graphics Module**: Handles rendering and visual effects with dependencies on Mesa 3D Graphics Library and Independent JPEG Group libraries
- **JavaFX Media Module**: Includes GStreamer, DirectShow, GLib, and LibFFI for potential multimedia support
- **JavaFX Web Module**: Contains WebKit, ICU, libxml2, and libxslt libraries for web content rendering capabilities

## Third-Party Libraries
The JavaFX distribution includes several third-party components under various open-source licenses:
- **Mesa 3D Graphics**: Provides OpenGL rendering capabilities
- **GStreamer**: Media framework for audio/video processing
- **WebKit**: Web browser engine for HTML/CSS rendering
- **ICU (International Components for Unicode)**: Unicode and internationalization support
- **libxml2/libxslt**: XML parsing and transformation libraries

These dependencies suggest the application has the capability to extend beyond basic markdown editing to include multimedia content and web-based features, though the current implementation focuses primarily on text editing with syntax highlighting.
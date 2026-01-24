parser grammar KSMLParser;

import GLSLParser;

options {
	tokenVocab = KSMLLexer;
}

ksmlTranslationUnit : moduleMeta glVersionMeta requiresMeta* ksmlDeclaration* EOF;

moduleMeta : AT MODULE IDENTIFIER;

requiresMeta : AT REQUIRES IDENTIFIER; 

ksmlDeclaration :
    delcarationMetas
    external_declaration;
    
delcarationMetas :
  (exportMeta | glRequiresMeta)*;

exportMeta : AT EXPORT IDENTIFIER?;

glRequiresMeta : AT GL_REQUIRES VERSION_NUMBER;

glVersionMeta : AT GL_VERSION VERSION_NUMBER;



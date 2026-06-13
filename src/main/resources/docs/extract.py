import zipfile
import xml.etree.ElementTree as ET

def read_docx(file_path):
    try:
        doc = zipfile.ZipFile(file_path)
        xml_content = doc.read('word/document.xml')
        tree = ET.XML(xml_content)
        
        WORD_NAMESPACE = '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
        PARA = WORD_NAMESPACE + 'p'
        TEXT = WORD_NAMESPACE + 't'
        
        paragraphs = []
        for paragraph in tree.iter(PARA):
            texts = [node.text for node in paragraph.iter(TEXT) if node.text]
            if texts:
                paragraphs.append(''.join(texts))
        
        with open('extracted.txt', 'w', encoding='utf-8') as f:
            f.write('\n'.join(paragraphs))
    except Exception as e:
        with open('error.txt', 'w') as f:
            f.write(str(e))

read_docx(r'd:\Workspace\Nam3_Ky2_Dot2\DesignPattern\BaoCaoCuoiKy\ClothingStore\backend\src\main\resources\docs\Nhom10_FinalProject1.docx')

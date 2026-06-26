import pandas as pd
df = pd.read_excel(r'd:\Workspace\Nam3_Ky2_Dot2\DesignPattern\BaoCaoCuoiKy\ClothingStore\backend\src\main\resources\docs\Bảng công việc design pattern.xlsx')
df.to_csv('output.csv', index=False, encoding='utf-8')

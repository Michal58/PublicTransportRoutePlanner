import pandas as pd
from io import StringIO

df = pd.read_csv("data\\connection_graph.csv", index_col=0)

# print(df)

print(len(df['start_stop'].unique()))

df = df.drop_duplicates(subset=None)

# print(df)
cols_to_check = ['line','departure_time','arrival_time','start_stop','end_stop']
# duplicate_rows = df[df.duplicated(subset=cols_to_check, keep=False)]
# print(duplicate_rows)

df = df.drop_duplicates(subset=cols_to_check)

# there are only minutes in schedule

print(len(df))
print(df.columns)

df = df.drop('company',axis=1)

df.to_csv('.\\refactoerd_graph.csv',index=False)

# coding:utf-8

# 参考代码：https://www.cnblogs.com/pinard/p/9220199.html
# 注意:sklearn生成的pmml文件可能版本过新，与Javapmml不兼容，可以修改pmml文件第一行版本号（比如将4.4改为4.3）
from sklearn import tree
from sklearn2pmml.pipeline import PMMLPipeline
from sklearn2pmml import sklearn2pmml

X=[[1,2,3,1],[2,4,1,5],[7,8,3,6],[4,8,4,7],[2,5,6,9]]
y=[0,1,0,2,1]
pipeline = PMMLPipeline([("classifier", tree.DecisionTreeClassifier(random_state=9))]);
pipeline.fit(X,y)

sklearn2pmml(pipeline, "/Users/youshu_/Java_Workspace/Java_function_demo/src/main/java/pmml/sklearn2pmml/model/sklearn.pmml", with_repr = True)
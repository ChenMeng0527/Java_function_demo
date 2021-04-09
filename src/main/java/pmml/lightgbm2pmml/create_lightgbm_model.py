from sklearn.datasets import load_boston
import numpy as np
boston = load_boston()
data = np.array(boston['data'])[:,0:3]
y = boston['target']

from lightgbm import LGBMRegressor

lgbm = LGBMRegressor(objective = "regression")
lgbm.fit( data, y,feature_name=['A', 'B', 'C'] )

lgbm.booster_.save_model("/Users/youshu_/Python_Workspace/ML_model/Xgb/lightgbm.txt")

# java -jar jpmml-lightgbm-executable-1.3-SNAPSHOT.jar  \
#      --lgbm-input /Users/youshu_/Python_Workspace/ML_model/Xgb/lightgbm.txt  \
#      --pmml-output /Users/youshu_/Python_Workspace/ML_model/Xgb/lightgbm.pmml
# JVM 常见问题 3

# 1. 类加载机制

双亲委派机制，子类在加载类的时候会将类交给父类去加载，只有在父类无法加载的时候，才会交给子类去加载。
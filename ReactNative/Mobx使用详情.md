# Mobx 使用详情

# 1. 简介

　　Mobx 是一个功能强大、上手非常容易的状态管理工具，在不少情况下可以使用 Mobx 来替代掉 redux。

## 1.1. observable 和 autorun

```js
import { observable, autorun } from 'mobx';

const value = observable(0);
const number = observable(100);

autorun(() => {
  console.log(value.get());
});

value.set(1);
value.set(2);
number.set(101);

```

　　控制台依次输出 0,1,2。

　　observable 可以用来观测一个数据，这个数据可以数字、字符串、数组、对象类型，而当观测到的数据发生变化时的时候，如果变化的值处在 autorun 中，那么 autorun 就会自动执行。

## 1.2. action、runInAction 和严格模式（useStrict）

　　mobx 推荐将修改被观测变量的行为放在 action 中。

```js
import {observable, action} from 'mobx';
class Store {
  @observable number = 0;
  @action add = () => {
    this.number++;
  }
}

const newStore = new Store();
newStore.add();
```

　　就算把 @action 去掉，程序还是可以运行的，这是因为使用的 Mobx 的非严格模式，如果在严格模式下，就会报错了。

　　Mobx 里启用严格模式的函数时 useStrict。

```js
useStrict(true);
```

　　实际开发的时候建议开启严格模式，这样不至于在各个地方很轻易的改变所需要的值，降低不确定性。

　　action 的写法大概有如下几种：

* action(fn)
* action(name,fn)
* @action classMethod() {}
* @action(name) classMethod(){}
* @action boundClassMethod=(args) =>{body}
* @action(name) boundClassMethod = (args) => {body}
* @action.bound classMethod() {}
* @action.bound(function() {})

　　action 只能影响正在运行的函数，而无法影响当前函数调用的异步操作。

```js
@action createRandomContact() {
  this.pendingRequestCount++;
  superagent
    .get('https://randomuser.me/api/')
    .set('Accept', 'application/json')
    .end(action("createRandomContact-callback", (error, results) => {
      if (error)
        console.error(error);
      else {
        const data = JSON.parse(results.text).results[0];
        const contact = new Contact(this, data.dob, data.name, data.login.username, data.picture);
        contact.addTag('random-user');
        this.contacts.push(contact);
        this.pendingRequestCount--;
      }
  }));
}
```

　　在 end 中触发的回调函数，被 action 给包裹了，action 无法影响当前函数调用的异步操作，而这个回调毫无疑问是一个异步操作，所以必须再用一个 action 来包裹住它，这样程序才不会报错。

　　如果使用 async function 来处理业务，那么可以使用 runAction 这个 API 来解决之前的问题。

```js
import {observable, action, useStrict, runInAction} from 'mobx';
useStrict(true);

class Store {
  @observable name = '';
  @action load = async () => {
    const data = await getData();
    runInAction(() => {
      this.name = data.name;
    });
  }
}
```

# 2. 结合 React 使用

　　在 React 中，一般会把和页面相关的数据放到 state 中，在需要改变这些数据的时候，会去用 setState 这个方法来进行改变。

　　使用 Mobx 处理一个简单的场景，页面上有个数字 0 和一个按钮，点击按钮让这个数字增加 1。

```js
import React from 'react';
import { observable, useStrict, action } from 'mobx';
import { observer } from 'mobx-react';
useStrict(true);

class MyState {
  @observable num = 0;
  @action addNum = () => {
    this.num++;
  };
}

const newState = new MyState();

@observer
export default class App extends React.Component {

  render() {
    return (
      <div>
        <p>{newState.num}</p>
        <button onClick={newState.addNum}>+1</button>
      </div>
    )
  }
}

```

　　在上面使用了一个 MyState 类，在这个类中定义了一个被观测的 num 变量和一个 action 函数 addNum 来改变这个 num 值。

　　之后实例化一个对象，叫做 newState，之后在 React 组件中，只需要用 @observer 修饰一下组件类，便可以愉悦地使用这个 newState 对象中的值和函数了。

## 2.1. 跨组件交互

　　在不使用其他框架、类库的情况下，React 要实现跨组件交互这一功能相对有些繁琐。通常需要在父组件上定义一个 state 和一个修改该 state 的函数。然后把 state 和这个函数分别传到两个子组件里，在逻辑简单，且子组件很少的时候可能还好，但当业务复杂起来后，这么写就非常繁琐，且难以维护。

　　而用 Mobx 就可以很好地解决这个问题：

```js
class MyState {
  @observable num1 = 0;
  @observable num2 = 100;

  @action addNum1 = () => {
    this.num1 ++;
  };
  @action addNum2 = () => {
    this.num2 ++;
  };
  @computed get total() {
    return this.num1 + this.num2;
  }
}

const newState = new MyState();

const AllNum = observer((props) => <div>num1 + num2 = {props.store.total}</div>);

const Main = observer((props) => (
  <div>
    <p>num1 = {props.store.num1}</p>
    <p>num2 = {props.store.num2}</p>
    <div>
      <button onClick={props.store.addNum1}>num1 + 1</button>
      <button onClick={props.store.addNum2}>num2 + 1</button>
    </div>
  </div>
));

@observer
export default class App extends React.Component {

  render() {
    return (
      <div>
        <Main store={newState} />
        <AllNum store={newState} />
      </div>
    );
  }
}
```

　　有两个子组件，Main 和 AllNum（均采用无状态函数的方式声明的组件）。在 MyState 中存放了这些组件要用到的所有状态和函数。

　　之后只要在父组件需要的地方实例化一个 MyState 对象，需要用到数据的子组件，只需要将这个实例化的对象通过 props 传下去就好了。

　　如果组件树比较深，可以借助 React 15 版本的新特性 context 来完成。它可以将父组件

## 2.2. 网络请求



# 3. Observable Object 和 Observable Arrays

## 3.1. Observable Objects



## 3.2. Observable Arrays





# 参考文档

1. [Mobx使用详解](https://www.jianshu.com/p/505d9d9fe36a)


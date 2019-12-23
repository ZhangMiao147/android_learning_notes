# 关于 Intent 的全部 flags 介绍

## 简介

　　除了 4 种 launchMode 可以设置 Activity 与 task 的关系之外，还可以通过 startActivity 方法的 Intent 添加 Flag 来设置 Activity 与 task 的关系。本片只是将 20 个 flags 罗列出来，并简单介绍描述。

## 20 个 flag 的描述

#### 1. FLAG_ACTIVITY_CLEAR_TASK
```
    /**
     * If set in an Intent passed to {@link Context#startActivity Context.startActivity()},
     * this flag will cause any existing task that would be associated with the
     * activity to be cleared before the activity is started.  That is, the activity
     * becomes the new root of an otherwise empty task, and any old activities
     * are finished.  This can only be used in conjunction with {@link #FLAG_ACTIVITY_NEW_TASK}.
     */
```
　　通过 Context.startActivity() 的 Intent 设置这个 FLAG_ACTIVITY_CLEAR_TASK 标志，将会在 activity 启动之前清除与 activty 有关联的存在的全部任务。也就是说，activity 将成为一个空任务栈的新根，任何旧的 activities 将结束。这个标志只能与 FLAG_ACTIVITY_NEW_TASK 一起使用。

#### 2. FLAG_ACTIVITY_CLEAR_TOP
```
    /**
     * If set, and the activity being launched is already running in the
     * current task, then instead of launching a new instance of that activity,
     * all of the other activities on top of it will be closed and this Intent
     * will be delivered to the (now on top) old activity as a new Intent.
     *
     * <p>For example, consider a task consisting of the activities: A, B, C, D.
     * If D calls startActivity() with an Intent that resolves to the component
     * of activity B, then C and D will be finished and B receive the given
     * Intent, resulting in the stack now being: A, B.
     *
     * <p>The currently running instance of activity B in the above example will
     * either receive the new intent you are starting here in its
     * onNewIntent() method, or be itself finished and restarted with the
     * new intent.  If it has declared its launch mode to be "multiple" (the
     * default) and you have not set {@link #FLAG_ACTIVITY_SINGLE_TOP} in
     * the same intent, then it will be finished and re-created; for all other
     * launch modes or if {@link #FLAG_ACTIVITY_SINGLE_TOP} is set then this
     * Intent will be delivered to the current instance's onNewIntent().
     *
     * <p>This launch mode can also be used to good effect in conjunction with
     * {@link #FLAG_ACTIVITY_NEW_TASK}: if used to start the root activity
     * of a task, it will bring any currently running instance of that task
     * to the foreground, and then clear it to its root state.  This is
     * especially useful, for example, when launching an activity from the
     * notification manager.
     *
     * <p>See
     * <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and Back
     * Stack</a> for more information about tasks.
     */
```
　　如果设置 FLAG_ACTIVITY_CLEAR_TOP 标志，activity 已经在栈中，则不会启动 activity 的新实例，而是清除栈中 activity 之上的 activities，Intent 将被传递新的 Intent 给栈内的 activity。

　　例如，一个栈中包含 activities : A,B,C,D。如果 D 调用 startActivity() 去打开 activity B，并且 startActivity() 方法的 Intent 添加了 FLAG_ACTIVITY_CLEAR_TOP 标志，这时，c 和 D 出栈结束，B 会接收到给定的 Intent ，从而现在栈中是：A，B。

　　上述例子中当前运行的 activity B 的 onNewInstent() 方法将会收到新的 intent。如果将启动模式声明为 “multiple”(默认)，并且没有在 Intent 上设置 FLAG_ACTIVITY_SINGLE_TOP，它将结束并重新创建，除此之外的其他启动模式或者 Intent 设置 FLAG_ACTIVITY_SINGLE_TOP ，将传递 Intent 给当前的 activity 实例的 onNewIntent() 方法。

　　此 flag 还可以与 FLAG_ACTIVITY_NEW_TASK 一起使用起到更好的效果：如果被使用给栈的根 activity ，activity 会成为前台 activity，并且将其清除到根状态。这是特别有用的，比如，从通知管理器开启 activity 。

#### 3. FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
```
    /**
     * @deprecated As of API 21 this performs identically to
     * {@link #FLAG_ACTIVITY_NEW_DOCUMENT} which should be used instead of this.
     */

```
　　从 API 21 此 flag 被废弃，使用 FLAG_ACTIVITY_NEW_DOCUMENT 代替它。


#### 4. FLAG_ACTIVITY_MULTIPLE_TASK
```
    /**
     * This flag is used to create a new task and launch an activity into it.
     * This flag is always paired with either {@link #FLAG_ACTIVITY_NEW_DOCUMENT}
     * or {@link #FLAG_ACTIVITY_NEW_TASK}. In both cases these flags alone would
     * search through existing tasks for ones matching this Intent. Only if no such
     * task is found would a new task be created. When paired with
     * FLAG_ACTIVITY_MULTIPLE_TASK both of these behaviors are modified to skip
     * the search for a matching task and unconditionally start a new task.
     *
     * <strong>When used with {@link #FLAG_ACTIVITY_NEW_TASK} do not use this
     * flag unless you are implementing your own
     * top-level application launcher.</strong>  Used in conjunction with
     * {@link #FLAG_ACTIVITY_NEW_TASK} to disable the
     * behavior of bringing an existing task to the foreground.  When set,
     * a new task is <em>always</em> started to host the Activity for the
     * Intent, regardless of whether there is already an existing task running
     * the same thing.
     *
     * <p><strong>Because the default system does not include graphical task management,
     * you should not use this flag unless you provide some way for a user to
     * return back to the tasks you have launched.</strong>
     *
     * See {@link #FLAG_ACTIVITY_NEW_DOCUMENT} for details of this flag's use for
     * creating new document tasks.
     *
     * <p>This flag is ignored if one of {@link #FLAG_ACTIVITY_NEW_TASK} or
     * {@link #FLAG_ACTIVITY_NEW_DOCUMENT} is not also set.
     *
     * <p>See
     * <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and Back
     * Stack</a> for more information about tasks.
     *
     * @see #FLAG_ACTIVITY_NEW_DOCUMENT
     * @see #FLAG_ACTIVITY_NEW_TASK
     */
```
　　FLAG_ACTIVITY_MULTIPLE_TASK 标志用于创建新的任务，并在任务中启动 activity，FLAG_ACTIVITY_MULTIPLE_TASK 标志经常与 FLAG_ACTIVITY_NEW_DOCUMENT  或者 FLAG_ACTIVITY_NEW_TASK 一起使用。单独使用 FLAG_ACTIVITY_NEW_DOCUMENT 或 FLAG_ACTIVITY_NEW_TASK 时，会先从存在的栈中搜索匹配 Intent 的栈 ，如果没有栈被发现则创建新的栈。当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配的栈而是直接开启一个新栈。

　　如果使用了 FLAG_ACTIVITY_NEW_TASK 就不要使用此标志，除非你启动的是应用的 launcher 。当与 FLAG_ACTIVITY_NEW_TASK 组合使用时，会防止将已存在的任务带到前台，总是会为 activity 开启一个新的任务，不管是否已经存在任务正在运行相同的事情。

　　因为系统默认不会包含可视化的任务管理，因此不应该使用这个标志，除非给用户提供可以回到其他任务的方法。

　　此标志不与 FLAG_ACTIVITY_NEW_TASK 或者 FLAG_ACTIVITY_NEW_DOCUMENT 配合使用时是没有效的。

#### 5. FLAG_ACTIVITY_NEW_DOCUMENT
```
    /**
     * This flag is used to open a document into a new task rooted at the activity launched
     * by this Intent. Through the use of this flag, or its equivalent attribute,
     * {@link android.R.attr#documentLaunchMode} multiple instances of the same activity
     * containing different documents will appear in the recent tasks list.
     *
     * <p>The use of the activity attribute form of this,
     * {@link android.R.attr#documentLaunchMode}, is
     * preferred over the Intent flag described here. The attribute form allows the
     * Activity to specify multiple document behavior for all launchers of the Activity
     * whereas using this flag requires each Intent that launches the Activity to specify it.
     *
     * <p>Note that the default semantics of this flag w.r.t. whether the recents entry for
     * it is kept after the activity is finished is different than the use of
     * {@link #FLAG_ACTIVITY_NEW_TASK} and {@link android.R.attr#documentLaunchMode} -- if
     * this flag is being used to create a new recents entry, then by default that entry
     * will be removed once the activity is finished.  You can modify this behavior with
     * {@link #FLAG_ACTIVITY_RETAIN_IN_RECENTS}.
     *
     * <p>FLAG_ACTIVITY_NEW_DOCUMENT may be used in conjunction with {@link
     * #FLAG_ACTIVITY_MULTIPLE_TASK}. When used alone it is the
     * equivalent of the Activity manifest specifying {@link
     * android.R.attr#documentLaunchMode}="intoExisting". When used with
     * FLAG_ACTIVITY_MULTIPLE_TASK it is the equivalent of the Activity manifest specifying
     * {@link android.R.attr#documentLaunchMode}="always".
     *
     * Refer to {@link android.R.attr#documentLaunchMode} for more information.
     *
     * @see android.R.attr#documentLaunchMode
     * @see #FLAG_ACTIVITY_MULTIPLE_TASK
     */
```
　　FLAG_ACTIVITY_NEW_DOCUMENT 标志通过启动 activity 的 Intent 打开一个新的任务记录。通过使用这个标志或者它的同含义属性 android.R.attr#documentLaunchMode ，同一个 activity 的不同实例将会在最近的任务列表中显示不同的记录。

　　Intent 的标志要优先于对 activity 的属性使用 android.R.attr#documentLaunchMode。属性表单会为 Activity 的所有活动设置特殊的多记录行为，然而，标志需要在启动 Activity 的 Intent 上指定它。

　　注意，这个标志的默认含义，在 activity 结束之后它的入口是否还会保留在在最近的列表中与 FLAG_ACTIVITY_NEW_TASK 和 android.R.attr#documentLaunchMode 是不同的，此标志是不会被移除的，而 FLAG_ACTIVITY_NEW_TASK 则会被移除 。如果使用 FLAG_ACTIVITY_NEW_TASK 创建一个新的入口，那么默认情况下当 activity 结束时入口将被移除，你可以使用 FLAG_ACTIVITY_RETAIN_IN_RECENTS 标志修改这个行为。

　　FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 一起使用，当单独使用 FLAG_ACTIVITY_NEW_DOCUMENT 相当于在 manifest 中 Activity 定义 android.R.attr#docucumentLaunchMode  = “intoExisting”；当和 FLAG_ACTIVITY_MULTIPLE_TASK 一起使用时，相当于在 manifest 中 Activity 定义 android.R.attr#docucumentLaunchMode = “always”。

#### 6. FLAG_ACTIVITY_NEW_TASK
```
    /**
     * If set, this activity will become the start of a new task on this
     * history stack.  A task (from the activity that started it to the
     * next task activity) defines an atomic group of activities that the
     * user can move to.  Tasks can be moved to the foreground and background;
     * all of the activities inside of a particular task always remain in
     * the same order.  See
     * <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and Back
     * Stack</a> for more information about tasks.
     *
     * <p>This flag is generally used by activities that want
     * to present a "launcher" style behavior: they give the user a list of
     * separate things that can be done, which otherwise run completely
     * independently of the activity launching them.
     *
     * <p>When using this flag, if a task is already running for the activity
     * you are now starting, then a new activity will not be started; instead,
     * the current task will simply be brought to the front of the screen with
     * the state it was last in.  See {@link #FLAG_ACTIVITY_MULTIPLE_TASK} for a flag
     * to disable this behavior.
     *
     * <p>This flag can not be used when the caller is requesting a result from
     * the activity being launched.
     */
```
　　设置 FLAG_ACTIVITY_NEW_TASK 标志，activity 将成为历史栈中的新任务的开始。一个任务栈（从启动它的 activity 到下一个 activity）定义了一个用户操作的 activities 的原子组。栈可以被移动到前台和后台；包含 activity 的任务栈的所有 activities 总是保持相同的顺序。

　　此标志通常被用于想要显示 “launcher” 类型行为的 activities，用户完成一系列的操作，并完全独立于启动他们的 activity 。

　　当使用这个标志时，如果栈中已经包含了想要启动的 activity，那么，并不会启动一个新的 activity，而是将包含 activity 的任务栈移到屏幕的前面，并显示栈的最后一次的状态。可以使用 FLAG_ACTIVITY_MULTIPLE_TASK 标志禁止这种行为。

　　当调用者 activity 想要从启动的 activity 请求到 result 时，这个标志不能使用。

#### 7. FLAG_ACTIVITY_NO_ANIMATION
```
    /**
     * If set in an Intent passed to {@link Context#startActivity Context.startActivity()},
     * this flag will prevent the system from applying an activity transition
     * animation to go to the next activity state.  This doesn't mean an
     * animation will never run -- if another activity change happens that doesn't
     * specify this flag before the activity started here is displayed, then
     * that transition will be used.  This flag can be put to good use
     * when you are going to do a series of activity operations but the
     * animation seen by the user shouldn't be driven by the first activity
     * change but rather a later one.
     */
```

　　如果在 Context.startActivity 给 Intent 设置 FLAG_ACTIVITY_NO_ANIMATION 标志，将不会展示系统在 activity 从当前状态跳转到下一个 activity 的切换动画。这不意味着动画将不会被展示，如果另一个 activity 的改变在当前展示的动画前发生并且没有使用这个 flag，那么动画还是会展示（不会影响其他 activity 的切换动画的展示）。当你进行一系列的 activity 操作时，用户看到的动画就不应由第一个 activity  的改变而展示而是由后一个展示。

#### 8. FLAG_ACTIVITY_NO_HISTORY
```
    /**
     * If set, the new activity is not kept in the history stack.  As soon as
     * the user navigates away from it, the activity is finished.  This may also
     * be set with the {@link android.R.styleable#AndroidManifestActivity_noHistory
     * noHistory} attribute.
     *
     * <p>If set, {@link android.app.Activity#onActivityResult onActivityResult()}
     * is never invoked when the current activity starts a new activity which
     * sets a result and finishes.
     */
```
　　如果设置 FLAG_ACTIVITY_NO_HISTORY 标志，则新的 activity 将不会保留在历史栈中。一旦用户离开它，activity 将结束。也可以通过 `android.R.styleable#AndroidManifestActivity_noHistory noHistory` 属性设置。

　　如果设置 FLAG_ACTIVITY_NO_HISTORY 标志，当前的 activity 启动一个新的 activity ，新的 activity 设置返回值并结束，但旧的 activity 的 onActivityResult() 不会被触发。

#### 9. FLAG_ACTIVITY_REORDER_TO_FRONT
```
    /**
     * If set in an Intent passed to {@link Context#startActivity Context.startActivity()},
     * this flag will cause the launched activity to be brought to the front of its
     * task's history stack if it is already running.
     *
     * <p>For example, consider a task consisting of four activities: A, B, C, D.
     * If D calls startActivity() with an Intent that resolves to the component
     * of activity B, then B will be brought to the front of the history stack,
     * with this resulting order:  A, C, D, B.
     *
     * This flag will be ignored if {@link #FLAG_ACTIVITY_CLEAR_TOP} is also
     * specified.
     */
```
　　如果通过 Context.startActivity() 的 Intent 设置 FLAG_ACTIVITY_REORDER_TO_FRONT 标志，如果 activity 已经在栈中运行，将会把 activity 带到栈的顶部。

　　例如。一个栈中包含四个 activities：A,B,C,D。如果 D 调用 startActivity() 方法，并且 Intent 设置 FLAG_ACTIVITY_REORDER_TO_FRONT 标志，启动 B，那么 B 将被带到历史栈的栈顶，所以其结果就是：A,C,D,B。

　　如果此标志与 FLAG_ACTIVITY_CLEAR_TOP 同时设置，那么此标志无效。

#### 10. FLAG_ACTIVITY_RETAIN_IN_RECENTS
```
    /**
     * By default a document created by {@link #FLAG_ACTIVITY_NEW_DOCUMENT} will
     * have its entry in recent tasks removed when the user closes it (with back
     * or however else it may finish()). If you would like to instead allow the
     * document to be kept in recents so that it can be re-launched, you can use
     * this flag. When set and the task's activity is finished, the recents
     * entry will remain in the interface for the user to re-launch it, like a
     * recents entry for a top-level application.
     * <p>
     * The receiving activity can override this request with
     * {@link android.R.attr#autoRemoveFromRecents} or by explcitly calling
     * {@link android.app.Activity#finishAndRemoveTask()
     * Activity.finishAndRemoveTask()}.
     */
```
　　默认情况下，进入最近任务栈的记录由 FLAG_ACTIVITY_NEW_DOCUMENT 创建，当用户关闭 activity （使用 back 键或者他调用 finish()）时记录将会被移除，如果你想要允许记录保留在最近方便它能被重新启动，你可以使用 FLAG_ACTIVITY_RETAIN_IN_RECENTS 标志。当设置了此标志并且任务中的 activity 已经结束，当用户重新启动它，界面将会保留最近的进入记录，就像是顶部应用进入一样。

　　接收活动可以请求 android.R.attr#autoRemoveFromRecents 或者通过调用 Activity.finishAndRemoveTask() 来覆盖本请求。

#### 11. FLAG_ACTIVITY_SINGLE_TOP
```
    /**
     * If set, the activity will not be launched if it is already running
     * at the top of the history stack.
     */
```
　　如果设置 FLAG_ACTIVITY_SINGLE_TOP 标志，当 activity 已经运行在历史栈的顶端，则 activity 将不会被启动。

#### 12. FLAG_ACTIVITY_FORWARD_RESULT
```
    /**
     * If set and this intent is being used to launch a new activity from an
     * existing one, then the reply target of the existing activity will be
     * transfered to the new activity.  This way the new activity can call
     * {@link android.app.Activity#setResult} and have that result sent back to
     * the reply target of the original activity.
     */
```
　　如果设置 FLAG_ACTIVITY_FORWARD_RESULT 标志并用于启动一个新的 activity，则启动活动的回复转移到被启动的活动上。

　　被启动活动调用 android.app.Activity#setResult 方法，这个结果数据将会返回给启动 activity 的启动 Activity。

#### 13. FLAG_ACTIVITY_PREVIOUS_IS_TOP
```
    /**
     * If set and this intent is being used to launch a new activity from an
     * existing one, the current activity will not be counted as the top
     * activity for deciding whether the new intent should be delivered to
     * the top instead of starting a new one.  The previous activity will
     * be used as the top, with the assumption being that the current activity
     * will finish itself immediately.
     */
```
　　如果设置 FLAG_ACTIVITY_PREVIOUS_IS_TOP 标志，并且用于启动一个新的 activity ，当前 activity 不会被视为栈顶活动，无论是传递新的 intent 给栈顶还是启动一个新的 activity 。如果当前的 activity 将立即结束，则上一个 activity 将作为栈顶。

#### 14. FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
```
    /**
     * If set, the new activity is not kept in the list of recently launched
     * activities.
     */
```
　　如果设置 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 标志，则新的 activity 将不会被保留在最近启动 activities 的列表中。

#### 15. FLAG_ACTIVITY_BROUGHT_TO_FRONT
```
    /**
     * This flag is not normally set by application code, but set for you by
     * the system as described in the
     * {@link android.R.styleable#AndroidManifestActivity_launchMode
     * launchMode} documentation for the singleTask mode.
     */
```
　　FLAG_ACTIVITY_BROUGHT_TO_FRONT 通常不由应用代码设置，当 launchMode 为 singleTask 模式时由系统设置。

#### 16. FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
```
    /**
     * If set, and this activity is either being started in a new task or
     * bringing to the top an existing task, then it will be launched as
     * the front door of the task.  This will result in the application of
     * any affinities needed to have that task in the proper state (either
     * moving activities to or from it), or simply resetting that task to
     * its initial state if needed.
     */
```
　　如果设置 FLAG_ACTIVITY_RESET_TASK_IF_NEEDED 标志，activity 要么在新的任务中被启动要么将存在的 activity 移到存在任务的顶部，而 activity 将作为任务的前门被启动。这将导致与应用相关联的活动在适当的状态下需要拥有这个任务（无论是移动活动进入或者是移除），或者在需要的时候重置任务到初始状态。

#### 17. FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
```
    /**
     * This flag is not normally set by application code, but set for you by
     * the system if this activity is being launched from history
     * (longpress home key).
     */
```
　　FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY 标志通常不由应用代码设置，当 activity 从历史记录中启动（长按 home 键）时，系统就会为你设置。

#### 18. FLAG_ACTIVITY_NO_USER_ACTION
```
    /**
     * If set, this flag will prevent the normal {@link android.app.Activity#onUserLeaveHint}
     * callback from occurring on the current frontmost activity before it is
     * paused as the newly-started activity is brought to the front.
     *
     * <p>Typically, an activity can rely on that callback to indicate that an
     * explicit user action has caused their activity to be moved out of the
     * foreground. The callback marks an appropriate point in the activity's
     * lifecycle for it to dismiss any notifications that it intends to display
     * "until the user has seen them," such as a blinking LED.
     *
     * <p>If an activity is ever started via any non-user-driven events such as
     * phone-call receipt or an alarm handler, this flag should be passed to {@link
     * Context#startActivity Context.startActivity}, ensuring that the pausing
     * activity does not think the user has acknowledged its notification.
     */
```
　　如果设置 FLAG_ACTIVITY_NO_USER_ACTION 标志，在 activity 被前台的新启动的 activity 造成 paused 之前，将会阻止当前最顶部的 activity 的 onUserLeaveHint 回调。

　　通常，当 activity 在用户的操作下被移除前面则会调用 onUserLeaveHint 回调。这个回调标志着 activity 的生命周期的一个点，以便隐藏任何 “ 直到用户看到他们 ” 的通知，比如闪烁的 LED 灯。

　　如果 accitivity 曾经通过任何非用户操作启动（例如来电或闹铃启动），就应该通过 startActivity 添加此标志，确保暂停时 activity 不认为用户确认了它的通知。

#### 19. FLAG_ACTIVITY_TASK_ON_HOME
```
    /**
     * If set in an Intent passed to {@link Context#startActivity Context.startActivity()},
     * this flag will cause a newly launching task to be placed on top of the current
     * home activity task (if there is one).  That is, pressing back from the task
     * will always return the user to home even if that was not the last activity they
     * saw.   This can only be used in conjunction with {@link #FLAG_ACTIVITY_NEW_TASK}.
     */
```
　　如果通过 startActivity 的 Intent 设置 FLAG_ACTIVITY_TASK_ON_HOME 标志，这个标志将会导致最新启动的任务位于当前主页活动任务（假设这里有）的顶部。换句话说，当任务点击 back 键，将总是返回用户的主页，无论主页是否是用户看到的上一个界面。此标志只能与 FLAG_ACTIVITY_NEW_TASK 一起使用。

#### 20. FLAG_ACTIVITY_LAUNCH_ADJACENT
```
    /**
     * This flag is only used in split-screen multi-window mode. The new activity will be displayed
     * adjacent to the one launching it. This can only be used in conjunction with
     * {@link #FLAG_ACTIVITY_NEW_TASK}. Also, setting {@link #FLAG_ACTIVITY_MULTIPLE_TASK} is
     * required if you want a new instance of an existing activity to be created.
     */
```
　　FLAG_ACTIVITY_LAUNCH_ADJACENT 标志仅用于分屏多窗口模式。新活动将被显示在启动它的活动的旁边。这个标志只能与 FLAG_ACTIVITY_NEW_TASK 联合使用。此外，如果想要创建一个已存在的活动的新实例，那么设置 FLAG_ACTIVITY_MULTIPLE_TASK 标志。

## 验证
　　关于 flag 的验证，可以查看 [验证Intent的flags](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Intent%E7%9A%84flags.md) 文章。

## 参考文章：
1. [Intent.addFlags() 启动Activity的20种flags全解析](https://blog.csdn.net/blueangle17/article/details/79712229)
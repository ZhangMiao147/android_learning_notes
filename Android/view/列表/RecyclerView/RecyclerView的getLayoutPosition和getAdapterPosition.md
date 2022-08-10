# RecyclerView 的 getLayoutPosition 和 getAdapterPosition

- getLayoutPosition 和 getAdapterPosition 通常情况下是一样的，只有当 Adapter 里面的内容改变了，而 Layout 还没来得及绘制的这段时间之内才有可能不一样，这个时间小于16ms。
- 如果调用的是 notifyDataSetChanged()，因为要重新绘制所有 Item，所以在绘制完成之前 RecyclerView 是不知道 adapterPosition 的，这时会返回-1（NO_POSITION）。
- 但如果用的是 notifyItemInserted(0)，那立即就能获取到正确的 adapterPosition，即使新的 Layout 还没绘制完成，比如之前是0的现在就会变成1，因为插入了0, 相当于 RecyclerView 提前帮你计算的，此时getLayoutPosition 还只能获取到旧的值。
- 总的来说，大多数情况下用 getAdapterPosition，只要不用 notifyDataSetChanged() 来刷新数据就总能立即获取到正确 position 值。


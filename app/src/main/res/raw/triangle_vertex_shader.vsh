// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader

// uniform mat4 vMVPMatrix; 定义了一个4x4的矩阵变量vMVPMatrix，
// 用于存储模型视图投影矩阵。uniform关键字表示该变量是一个全局变量，在渲染过程中所有顶点都共享同一个值。
uniform mat4 vMVPMatrix;
// attribute vec4 vPosition; 定义了一个4维向量变量vPosition，用于存储顶点位置信息。
// attribute关键字表示该变量是一个顶点属性，每个顶点都有自己的值。
attribute vec4 vPosition;
// 顶点着色器的入口函数，表示每个顶点都会执行这个函数
void main() {
    // the matrix must be included as a modifier of gl_Position
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    gl_Position = vMVPMatrix * vPosition;
}